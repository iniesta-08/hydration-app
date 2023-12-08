from flask import Flask, jsonify, request
from pymongo import MongoClient
from bson import ObjectId
from datetime import datetime
import logging
import firebase_admin
from firebase_admin import credentials, messaging
from flask_apscheduler import APScheduler

app = Flask(__name__)

client = MongoClient("mongodb://mongodb:27017/", username="root", password="example")
db = client["hydrofit"]
users_collection = db["users"]
timeseries_collections = ['exercise', 'heartrate', 'fitnesslvl', 'hydration', 'lastdrinkTime', 'stepcount', 'deviceToken', "wateramount"]
cred = credentials.Certificate("hydrofitnotifications-firebase-adminsdk-xlv6w-6fe1334603.json")

firebase_admin.initialize_app(cred)
PROJECT_ID = 'hydrofitnotifications'
BASE_URL = 'https://fcm.googleapis.com'
FCM_ENDPOINT = 'v1/projects/' + PROJECT_ID + '/messages:send'
FCM_URL = BASE_URL + '/' + FCM_ENDPOINT
SCOPES = ['https://www.googleapis.com/auth/firebase.messaging']


def create_collection_if_not_exists(name):
    if name not in db.list_collection_names(filter={'name': name}):
        logging.debug("creating timeseries collection: {}".format(name))
        db.create_collection(name=name, timeseries = {'timeField': 'timestamp', 'metaField': 'metadata'})

def check_create_timeseries_collections():
    for collection in timeseries_collections:
        create_collection_if_not_exists(collection)


def insertdata(collection, metadata, timestamp):
    logging.debug(f"func: insertdata metadata:{metadata} timestamp: {timestamp}")
    try:
        with client.start_sesstion as session:
            res = collection.insert_one({'timestamp': datetime.fromtimestamp(timestamp), 'metadata': metadata}, session=session)
            logging.debug(res)
            return None
    except Exception as e:
        return e
    
def get_user_id(name):
    logging.debug("func: get user id")
    result = list(users_collection.find({'username': name}))
    if len(result) == 0:
        return -1
    return result[0]['_id']

# Endpoint to create a new user
@app.route('/users', methods=['POST'])
def create_user():
    logging.debug("func: create_user")
    data = request.json
    if 'username' not in data:
        logging.debug("create_user: username not provided")
        return jsonify({'error': 'Username is required'}), 400

    new_user = {
        'username': data['username'],
    }

    try:
        # Start a transaction
        with client.start_session() as session:
            # Insert the new user data into the collection
            result = list(users_collection.find({'username': data['username']}))
            if len(result) > 0:
                return jsonify({'message': "user already exists"}), 200
            
            result = None
            result = users_collection.insert_one(new_user, session=session)
            logging.debug(result)
            users_collection.find_one({'_id': ObjectId(result.inserted_id)}, session=session)
            logging.debug("create_user: created new user: {}".format(data['username']))
            return jsonify({'message': 'User created successfully', 'user': data['username']}), 200
    except Exception as e:
        logging.debug("create_user: exception while creating user: {}, e: {}".format(data['username'], str(e)))
        return jsonify({'error': str(e)}), 500  
      
@app.route('/data/<user_name>/<collection>', methods=['POST'])
def add_data(user_name, collection):
    logging.debug(f'func: add_data, {collection}')
    if collection not in timeseries_collections:
        return jsonify({'route not found'}), 404
    
    if collection not in db.list_collection_names(filter={'name': collection}):
        create_collection_if_not_exists(collection)

    id = get_user_id(user_name)
    if id == -1:
        return jsonify({"message": "user doesn't exist"}), 400
    
    data = request.json
    dat = data.get(collection, None)
    timestamp = data.get('timestamp', None)

    logging.debug(f'data: {data} timestamp: {timestamp}')
    if dat is None or timestamp is None:
        return jsonify({'error': f'{collection} or timestamp not given'}), 400
    try:
        date_format = '%Y-%m-%d %H:%M:%S'
        db[collection].insert_one({'timestamp': datetime.strptime(timestamp, date_format), 'metadata': {collection: dat, 'user_id': id}})
    except Exception as e:
        return jsonify({"error": str(e), "message": dat}), 500
    return jsonify({"message": "added successfully"}), 200

@app.route('/data/<user_name>/<collection>', methods=['GET'])
def getLastNData(user_name, collection):
    logging.debug("func: get last N data")
    if collection not in timeseries_collections:
        return jsonify({'route not found'}), 404
    
    if collection not in db.list_collection_names(filter={'name': collection}):
        create_collection_if_not_exists(collection)

    id = get_user_id(user_name)
    if id == -1:
        return jsonify({"message": "user doesn't exist"}), 400
    
    data = request.json
    n = data.get('N', None)
    if n is None:
        n = 1
    n = min(int(n), 100)
    logging.debug(f"glnd: n: {n}")
    try:
        latest_values = list(db[collection].find({'metadata.user_id': id}).sort([("timestamp", -1)]).limit(n))
        res = []
        for i in latest_values:
            res.append({'timestamp': i['timestamp'].timestamp(), collection: i['metadata'][collection]})
        return jsonify({"data": res}), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route("/test", methods=['GET'])
def hw():
    return jsonify({"data": "hello-world"}), 200

@app.route('/data/<user_name>/fitness', methods=['POST'])
def add_fitness(user_name):
    collection = request.json
    logging.debug(f'func: add_fitness_data, {collection}')
    try:
        exercise = collection['exercise']
        stepcount = collection['stepcount']
        heartrate = collection['heartrate']
        timestamp = collection['timestamp']
    except Exception as e: 
        return jsonify({'route not found'}), 404
    fitness = calculate_fitness_level(exercise, heartrate, stepcount)
    try: 
        addCollection("exercise", user_name, exercise, timestamp)
        addCollection("stepcount", user_name, stepcount, timestamp)
        addCollection("heartrate", user_name, heartrate, timestamp)
        addCollection("fitnesslvl", user_name,fitness, timestamp)
    except Exception as e:
         return jsonify({'error': str(e)}), 400
    
    return jsonify({"message": "added successfully", "fitness": fitness}), 200

@app.route('/data/<user_name>/hydration', methods=['POST'])
def add_hydration(user_name):
    collection = request.json
    logging.debug(f'func: add_hydration_data, {collection}')
    try:
        lastdrinkTime = collection['lastdrinkTime']
        wateramount = collection['wateramount']
        timestamp = collection['timestamp']
    except Exception as e: 
        return jsonify({'route not found'}), 404
    
    hydration = calculate_hydration(wateramount, lastdrinkTime)
    try: 
        addCollection("lastdrinkTime", user_name, lastdrinkTime, timestamp)
        addCollection("wateramount", user_name, wateramount, timestamp)
        addCollection("hydration", user_name,hydration, timestamp)
    except Exception as e:
         return jsonify({'error': str(e)}), 400
    
    return jsonify({"message": "added successfully", "hydration": hydration }), 200


def addCollection(collection, user_name, data, timestamp):
    if collection not in db.list_collection_names(filter={'name': collection}):
        create_collection_if_not_exists(collection)

    id = get_user_id(user_name)
    if id == -1:
        raise ValueError("Invalid User")
      
    logging.debug(f'data: {data} timestamp: {timestamp}')
    if data is None or timestamp is None:
       raise ValueError(f'{collection} or timestamp not given')
    try:
        date_format ='%Y-%m-%d %H:%M:%S'
        db[collection].insert_one({'timestamp': datetime.strptime(timestamp, date_format), 'metadata': {collection: data, 'user_id': id}})
    except Exception as e:
        raise e

def calculate_fitness_level(exercise_duration, heart_rate, step_count):
    # Define weights for each metric (adjust these based on importance)
    duration_weight = 0.4
    heart_rate_weight = 0.3
    step_count_weight = 0.3

    # Normalize values to a scale of 0-100 (adjust max values based on expected ranges)
    normalized_duration = min(exercise_duration / 60, 1) * 100
    normalized_heart_rate = min(heart_rate / 150, 1) * 100
    normalized_step_count = min(step_count / 10000, 1) * 100

    # Calculate fitness level using the weighted sum with bounded adjustment
    fitness_level = duration_weight * normalized_duration + heart_rate_weight * normalized_heart_rate + step_count_weight * normalized_step_count
    
    return int(fitness_level)


def calculate_hydration(total_water_drunk, last_drink_time):
    # Define weights for each metric (adjust these based on importance)
    water_weight = 0.5
    time_weight = 0.4
    activity_weight = 0.1

    # Parse last drink time
    try:
        last_drink_datetime = datetime.strptime(last_drink_time, "%H-%M-%S")
    except ValueError:
        raise ValueError("Invalid last drink time format. Please use HH-MM-SS format.")

    # Calculate time difference in minutes
    time_since_last_drink = (datetime.now() - last_drink_datetime).total_seconds() / 60

    # Calculate time of day as a percentage
    current_time = datetime.now().time()
    day_percentage = (current_time.hour * 60 + current_time.minute) / (24 * 60)

    # Normalize values to a scale of 0-1 (adjust max values based on expected ranges)
    normalized_water = min(total_water_drunk / 3000, 1)
    normalized_time = 1 - min(time_since_last_drink / 8, 1)
    normalized_activity = 1 - day_percentage

    # Calculate hydration level using a weighted sum
    hydration_level = (
        water_weight * normalized_water +
        time_weight * normalized_time +
        activity_weight * normalized_activity
    )

    # Map the hydration level to a scale from 1 to 10
    hydration_score = int(hydration_level * 10)

    # Ensure the score is within the desired range
    return hydration_score


def calculate_and_notify():
    try:
        users = list(users_collection.find({}))
        for user in users:
            try:
                id = user['_id']

                # Get the latest fitness data
                fitness_data = list(db['fitnesslvl'].find({'metadata.user_id': id}).sort([("timestamp", -1)]).limit(1))
                if not fitness_data:
                    continue
                fitness = fitness_data[0]['metadata']['fitnesslvl']

                # Get the latest hydration data
                hydration_data = list(db['hydration'].find({'metadata.user_id': id}).sort([("timestamp", -1)]).limit(1))
                if not hydration_data:
                    continue
                hydration = hydration_data[0]['metadata']['hydration']

                device_token = list(db['deviceToken'].find({'metadata.user_id': id}).sort([("timestamp", -1)]).limit(1))
                if not device_token:
                    continue
                device_token = device_token[0]['metadata']['deviceToken']
                logging.debug(f"Job called {user}")

                message = messaging.Message(
                    data={
                        'fitness': f"{fitness}",
                        'hydration': f"{hydration*10}"
                    },
                    token=device_token
                )

                response = messaging.send(message)
                logging.debug(response)

            except Exception as e:
                # Handle the exception (print, log, etc.)
                logging.error(f"Error in calculate_and_notify: Error processing user {user}: {str(e)}")

    except Exception as e:
        # Handle the exception (print, log, etc.)
        logging.error(f"Error in calculate_and_notify: Error fetching users: {str(e)}")

scheduler = APScheduler()

# Add the job to the scheduler
scheduler.add_job(func=calculate_and_notify, id="my_job", trigger="interval", seconds=30*60)

# Start the scheduler
scheduler.start()
  

if __name__ == "__main__":
    logging.basicConfig(
        level=logging.DEBUG,
        format='%(asctime)s - %(levelname)s - %(message)s'
    )
    app.run(debug = True, host='0.0.0.0', port=5002)
