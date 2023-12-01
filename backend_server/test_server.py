from pymongo import MongoClient
from bson import ObjectId
from datetime import datetime
import logging
import requests
import unittest
import random
import json
import time

user = 'test-user'
client = MongoClient("mongodb://mongodb:27017/", username="root", password="example")
db = client["hydrofit"]
users_collection = db["users"]
timeseries_collections = ['exercise', 'heartrate', 'fitnesslvl', 'hydration', 'lastdrink', 'stepcount']

def get_user_id(name):
    logging.debug("func: get user id")
    result = list(users_collection.find({'username': name}))
    if len(result) == 0:
        return -1
    return result[0]['_id']

def test_createUser():
    response = requests.post("http://localhost:5000/users", headers={"Content-Type": "application/json"}, data=json.dumps({"username": user}))

    res = list(users_collection.find({'username': user}))
    assert response.status_code == 200
    assert len(res) != 0

def test_insert_data():
    for collection in timeseries_collections:
        val = random.randint(0,100)
        ts = int(datetime.utcnow().timestamp()*100)/100
        response = requests.post(f"http://localhost:5000/data/{user}/{collection}", headers={"Content-Type": "application/json"},
                                  data = json.dumps({collection: val, 'timestamp': ts}))

        d = {f"metadata.{collection}": val, 'timestamp': ts}
        res = db[collection].find({f"metadata.{collection}": val})
        res = list(res)

        assert response.status_code == 200
        assert len(res) != 0
        assert res[0]['metadata'][collection] == val

def test_get_n_data():
    
    collection = 'heartrate'
    n = random.randint(5,10)
    arr = []
    for _ in range(n):
        val = random.randint(500,1000)
        ts = int(datetime.utcnow().timestamp()*10)/10
        arr.append({'val': val, 'ts': ts})

        response = requests.post(f"http://localhost:5000/data/{user}/{collection}", headers={"Content-Type": "application/json"},
                                  data = json.dumps({collection: val, 'timestamp': ts}))
        
        assert response.status_code == 200
        time.sleep(0.2)
    
    response = requests.get(f'http://localhost:5000/data/{user}/{collection}', headers={"Content-Type": "application/json"}, data=json.dumps({"N": n}))

    
    arr = arr[::-1]
    assert response.status_code == 200
    varr = response.json()['data']

    assert len(arr) == len(varr)
    # for i in range(n):
    #     assert arr[i]['val'] == varr[i][collection] and arr[i]['ts'] == varr[i]['timestamp']

def test_cleanup():

    id = get_user_id(user)
    for collection in timeseries_collections:
        db[collection].delete_many({'metadata.user_id': id})
    
    users_collection.delete_many({'username': user})
    


