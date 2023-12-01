from flask import Flask, jsonify, request
from pymongo import MongoClient
from bson import ObjectId
from datetime import datetime
import logging

app = Flask(__name__)

client = MongoClient("mongodb://mongodb:27017/", username="root", password="example")
db = client["hydrofit"]
users_collection = db["users"]
timeseries_collections = ['exercise', 'heartrate', 'fitnesslvl', 'hydration', 'lastdrink', 'stepcount']

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
        dat = int(dat)
        db[collection].insert_one({'timestamp': datetime.fromtimestamp(timestamp), 'metadata': {collection: dat, 'user_id': id}})
    except Exception as e:
        return jsonify({"error": str(e)}), 500
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

@app.route("/hello-wolrd", methods=['GET'])
def hw():
    return jsonify({"data": "hello-world"}), 200

if __name__ == "__main__":
    logging.basicConfig(
        level=logging.DEBUG,
        format='%(asctime)s - %(levelname)s - %(message)s'
    )
    app.run(host='0.0.0.0')
