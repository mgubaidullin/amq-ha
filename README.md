# AMQ HA in OpenShift Demo

## How to
### Prerequisites
OpenShift 4.7 up and running


### Deployment
Login to OpenShift
```bash
oc login https://host:port
oc project amq-ha
```

Create namespace and install operator
```
oc apply -k infra/cluster
```
Create AMQ Cluster
```
oc apply -k infra/amq
```
Deploy applications (producers and consumers)
```
oc apply -k infra/application
```

Show consumers logs
```
oc get pods --no-headers --output='json' -l app.kubernetes.io/part-of=amq-consumer -o=jsonpath='{.items[0].metadata.name}' | xargs oc logs -f
oc get pods --no-headers --output='json' -l app.kubernetes.io/part-of=amq-consumer -o=jsonpath='{.items[1].metadata.name}' | xargs oc logs -f
oc get pods --no-headers --output='json' -l app.kubernetes.io/part-of=amq-consumer -o=jsonpath='{.items[2].metadata.name}' | xargs oc logs -f
```

Kill broker pod
```
oc delete pod amq-ha-ss-0
```

Scale AMQ Cluster to 2 nodes throught UI
