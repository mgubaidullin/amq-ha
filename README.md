# AMQ Pod Draining in OpenShift Demo

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
Wait while 3 AMQ Brokers are up and running and deploy applications (1 producers and 1 consumer1)
```
oc apply -k infra/application
```

### Demo
1. Check AMQ Console to check that 1000 messages are in each broker
2. Scale down AQM Cluster to 2 brokers through OpenShift UI
3. Check pods in namespace (One terminated and then drawniing start/complete)
4. Check that 2 brokers have same 3000 messages
5. Start consumer through OpenShift UI
6. Show consumers logs
```
oc get pods --no-headers --output='json' -l app.kubernetes.io/part-of=amq-consumer -o=jsonpath='{.items[0].metadata.name}' | xargs oc logs -f
```
Check that all 3000 messages are received
