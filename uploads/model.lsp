use json;
use math;
use geodata;
use datetime;

function input() {
    data = json.parse(inFileName);
    nbCustomers = data.customers.count();
    nbTrucks = data.trucks.count();
    computeDistanceBetweenCustomers();
    computeDistanceCustomerDepot();
}

function model() {
    customerServiceTime <- array[customer in data.customers](customer.serviceTime);
    customerEarliestStart <- array[customer in data.customers](customer.timeWindowStart);
    customerLatestEnd <- array[customer in data.customers](customer.timeWindowEnd);
    customerDemand <- array[customer in data.customers](customer.demand);
    customerSequences[k in 0...nbTrucks] <- list(nbCustomers);
    constraint partition[k in 0...nbTrucks](customerSequences[k]);
    for[k in 0...nbTrucks] {
        local sequence <- customerSequences[k];
        local l <- count(sequence);
        routeUsed[k] <- l > 0;
        routeQuantity[k] <- sum(0...l, i => customerDemand[sequence[i]]);
        constraint routeQuantity[k] <= data.truckCapacity;
        routeDistance[k] <- sum(1...l, i => distanceBetweenCustomers[sequence[i-1]][sequence[i]]) + (routeUsed[k] ? distanceFromDepot[0][sequence[0]] + distanceToDepot[sequence[l-1]][0] : 0);
        routeEndTime[k] <- array(0...l, (i, prev) => max(customerEarliestStart[sequence[i]], i == 0 ? duration(distanceFromDepot[0][sequence[0]]) : prev + duration(distanceBetweenCustomers[sequence[i-1]][sequence[i]])) + customerServiceTime[sequence[i]]);
        routeFinalTime[k] <- routeUsed[k] ? routeEndTime[k][l-1] + duration(distanceToDepot[sequence[l-1]][0]) : 0;
        homeLateness[k] <- max(0, routeFinalTime[k] - data.endHorizon);
        lateness[k] <- homeLateness[k] + sum(0...l, i => max(0, routeEndTime[k][i] - customerLatestEnd[sequence[i]]));
    }
    totalNbRoutesUsed <- sum[k in 0...nbTrucks](routeUsed[k]);
    totalDistance <- sum[k in 0...nbTrucks](routeDistance[k]);
    totalLateness <- sum[k in 0...nbTrucks](lateness[k]);
    minimize totalLateness;
    minimize totalNbRoutesUsed;
    minimize totalDistance;
}

function param() {
    if(lsTimeLimit == nil) {
        lsTimeLimit = 20;
    }
}

function output() {
    outputNbTours = totalNbRoutesUsed.value;
    outputDistance = (round(100 * totalDistance.value) / 100) + " km";
    outputLateness = printTimespan(totalLateness.value);
    outputPoints = {};
    outputPointsIndices = {};
    nbPoints = 0;
    println(outputDistance,"------------------", outputNbTours);
    for[c in 0...nbCustomers] {
        outputPoints.add({"name": data.customers[c].name, "index": c, "color": "#3f87d2", "latitude": data.customers[c].latitude, "longitude": data.customers[c].longitude, "isDepot": false});
        outputPointsIndices[data.customers[c].id] = nbPoints;
        nbPoints += 1;
    }
    outputPoints.add({"name": data.depotName, "index": 0, "color": "#731fd2", "latitude": data.depotLatitude, "longitude": data.depotLongitude, "isDepot": true});
    
    outputPointsIndices[data.depotId] = nbPoints;
    nbPoints += 1;
    outputRoutes = {};
    for[k in 0...nbTrucks] {
        if(!routeUsed[k].value) {
            continue;
        }
        route = {outputPointsIndices[data.depotId]};
        for[i in 0...customerSequences[k].value.count()] {
            customer = customerSequences[k].value[i];
            route.add(outputPointsIndices[data.customers[customer].id]);
        }
        route.add(outputPointsIndices[data.depotId]);
        outputRoutes.add(route);
        routeColor[route] = data.trucks[k].color;
    }
    outputTasks = {};
    for[k in 0...nbTrucks] {
        if(!routeUsed[k].value) {
            continue;
        }
        for[i in 0...customerSequences[k].value.count()] {
            outputTasks.add(toMilliseconds({"name": "Service " + data.customers[customerSequences[k].value[i]].name, "startTime": (routeEndTime[k].value[i] - customerServiceTime.value[customerSequences[k].value[i]]), "endTime": routeEndTime[k].value[i], "resource": "Truck " + k, "color": "#c7c4c4"}));
        }
        for[i in 0..customerSequences[k].value.count()] {
            local startRoute = i == 0 ? 0 : routeEndTime[k].value[i-1];
            local routeDuration = i == 0 ? duration(distanceFromDepot[0][customerSequences[k].value[i]]) : (i == customerSequences[k].value.count() ? duration(distanceToDepot[customerSequences[k].value[i-1]][0]) : duration(distanceBetweenCustomers[customerSequences[k].value[i-1]][customerSequences[k].value[i]]));
            local taskName = "Route from " + (i == 0 ? data.depotName : data.customers[customerSequences[k].value[i-1]].name) + " to " + (i == customerSequences[k].value.count() ? data.depotName : data.customers[customerSequences[k].value[i]].name);
            outputTasks.add(toMilliseconds({"name": taskName, "startTime": startRoute, "endTime": (startRoute + routeDuration), "resource": "Truck " + k, "color": data.trucks[k].color}));
           
           
        }
    }
    
}

function computeDistanceBetweenCustomers() {
    distanceBetweenCustomers = geodata.computeMatrix(data.customers, data.customers)["distances"];
    distanceBetweenCustomers[n1 in 0...nbCustomers][n2 in 0...nbCustomers] = round(distanceBetweenCustomers[n1][n2] / 1000);
}

function computeDistanceCustomerDepot() {
    depotSource[0] = {"latitude": data.depotLatitude, "longitude": data.depotLongitude};
    distanceFromDepot = geodata.computeMatrix(depotSource, data.customers)["distances"];
    distanceToDepot = geodata.computeMatrix(data.customers, depotSource)["distances"];
    distanceFromDepot[0][n in 0...nbCustomers] = round(distanceFromDepot[0][n] / 1000);
    distanceToDepot[n in 0...nbCustomers][0] = round(distanceToDepot[n][0] / 1000);
    println(distanceToDepot,"------------------\n", depotSource);
}

function duration(distance) {
    return round(distance / data.truckSpeed);
}

function printTimespan(objective) {
    timespan = datetime.span(0, 0, round(objective));
    if(timespan.totalDays >= 1) {
        return round(10 * timespan.totalDays) / 10 + " day(s)";
    }
    if(timespan.totalHours >= 1) {
        return round(10 * timespan.totalHours) / 10 + " h";
    }
    if(timespan.totalMinutes >= 1) {
        return round(10 * timespan.totalMinutes) / 10 + " min";
    }
    if(timespan.totalSeconds >= 1) {
        return round(10 * timespan.totalSeconds) / 10 + " sec";
    }
    return 0;
}

function toMilliseconds(task) {
    return {"name": task.name, "startTime": task.startTime * 1000, "endTime": task.endTime * 1000, "resource": task.resource, "color": task.color};
}
