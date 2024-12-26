## SSE Gatling strange behaviour


Project to demonstrate a strange interaction between Proxy based on Spring cloud gateway and Gatling during the SSE testing . 
Points:
- Proxy put all SSE content in network
- Gatling retrieve events except the last event according to the log records

Problem - why the last event is not consumed by gatling? \
in [SimulationWithProblem.java](src/gatling/java/sse/simulation/SimulationWithProblem.java) if I put `"id": "5"` as a stop condition, the simulation is never stops 

### Reproduction 

Run environment 
```bash
 ./gradlew testComposeUp
```
Run simulation with problem to access to mock without last event
```bash
./gradlew gatlingRun --simulation sse.simulation.SimulationWithProblem
```
In log of proxy you will see that proxy consumes:
```bash
event: userActivity
2024-12-26T12:18:08.150095676Z data: {"id": "1", "eventTime": "2021-09-01T12:00:00Z", "activityType": "productReview", "details": {"comment": "Great experience overall!", "mood": "POSITIVE", "rating": 0.90}}
2024-12-26T12:18:08.150107010Z 
2024-12-26T12:18:08.150109635Z data: {"id": "2", "eventTime": "2021-09-01T12:01:00Z", "activityType": "productReview", "details": {"comment": "Unsatisfied with the product quality.", "mood": "NEGATIVE", "rating": 0.70}}
2024-12-26T12:18:08.150204760Z 
2024-12-26T12:18:08.150216051Z data: {"id": "3", "eventTime": "2021-09-01T12:02:00Z", "activityType": "productReview", "details": {"comment": "Average performance, nothing special.", "mood": "NEUTRAL", "rating": 0.50}}
2024-12-26T12:18:08.150220885Z 
2024-12-26T12:18:08.150223301Z data: {"id": "4", "eventTime": "2021-09-01T12:03:00Z", "activityType": "productReview", "details": {"comment": "Best purchase I've made this year!", "mood": "POSITIVE", "rating": 0.95}}
2024-12-26T12:18:08.150226885Z 
2024-12-26T12:18:08.150228926Z data: {"id": "5", "eventTime": "2021-09-01T12:04:00Z", "activityType": "productReview", "details": {"comment": "Overpriced for the features offered.", "mood": "NEGATIVE", "rating": 0.30}}
```
In gatling's log you will see that gatling consumes:
```bash 
16:18:08.166 [DEBUG] i.g.h.a.s.f.SseStream - Received SSE event ServerSentEvent(Some(userActivity),Some({"id": "1", "eventTime": "2021-09-01T12:00:00Z", "activityType": "productReview", "details": {"comment": "Great experience overall!", "mood": "POSITIVE", "rating": 0.90}}),None,None) while in Open state. Propagating.
16:18:08.167 [DEBUG] i.g.h.a.s.f.SseIdleState - Received unmatched message={"event":"userActivity","data":"{\"id\": \"1\", \"eventTime\": \"2021-09-01T12:00:00Z\", \"activityType\": \"productReview\", \"details\": {\"comment\": \"Great experience overall!\", \"mood\": \"POSITIVE\", \"rating\": 0.90}}"}
16:18:08.168 [DEBUG] i.g.h.a.s.f.SseStream - Received SSE event ServerSentEvent(None,Some({"id": "2", "eventTime": "2021-09-01T12:01:00Z", "activityType": "productReview", "details": {"comment": "Unsatisfied with the product quality.", "mood": "NEGATIVE", "rating": 0.70}}),None,None) while in Open state. Propagating.
16:18:08.168 [DEBUG] i.g.h.a.s.f.SseIdleState - Received unmatched message={"data":"{\"id\": \"2\", \"eventTime\": \"2021-09-01T12:01:00Z\", \"activityType\": \"productReview\", \"details\": {\"comment\": \"Unsatisfied with the product quality.\", \"mood\": \"NEGATIVE\", \"rating\": 0.70}}"}
16:18:08.168 [DEBUG] i.g.h.a.s.f.SseStream - Received SSE event ServerSentEvent(None,Some({"id": "3", "eventTime": "2021-09-01T12:02:00Z", "activityType": "productReview", "details": {"comment": "Average performance, nothing special.", "mood": "NEUTRAL", "rating": 0.50}}),None,None) while in Open state. Propagating.
16:18:08.168 [DEBUG] i.g.h.a.s.f.SseIdleState - Received unmatched message={"data":"{\"id\": \"3\", \"eventTime\": \"2021-09-01T12:02:00Z\", \"activityType\": \"productReview\", \"details\": {\"comment\": \"Average performance, nothing special.\", \"mood\": \"NEUTRAL\", \"rating\": 0.50}}"}
16:18:08.168 [DEBUG] i.g.h.a.s.f.SseStream - Received SSE event ServerSentEvent(None,Some({"id": "4", "eventTime": "2021-09-01T12:03:00Z", "activityType": "productReview", "details": {"comment": "Best purchase I've made this year!", "mood": "POSITIVE", "rating": 0.95}}),None,None) while in Open state. Propagating.
16:18:08.168 [DEBUG] i.g.h.a.s.f.SseIdleState - Received unmatched message={"data":"{\"id\": \"4\", \"eventTime\": \"2021-09-01T12:03:00Z\", \"activityType\": \"productReview\", \"details\": {\"comment\": \"Best purchase I've made this year!\", \"mood\": \"POSITIVE\", \"rating\": 0.95}}"}
16:18:08.168 [DEBUG] i.g.h.a.s.f.SseStream - Server closed the stream while in state Open. Reconnecting.
```
During the execution this command you will all events without the last one.
```bash
./gradlew gatlingRun --simulation sse.simulation.SimulationWithoutProblem
```
Down the environment
```bash
./graldew testComposeDown
```
During the debug session I didn't find any troubles during the flush the body in Netty code 