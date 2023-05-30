Circuit breaker pattern uses to prevent server from cascading failure. 
It has 3 state:
1. close - if threshold exceed defined percent go to open state 
2. open - returns exceptions until timeout is over and switch to half-open
3. half-open - check if some numbers of requests are correct switch to close,
if any request returns error switch to open state again.

Resilience4j library calculates the failure threshold using ringbit buffer.
The state of circuitbreaker changes from CLOSED to OPEN when at end of count size the failure rate exceeds the threshold.

Resilience4j provides you two buffers - CLOSED and HALF_OPEN state buffers. Imagine this is our set up

Good explanation: https://www.javahabit.com/2020/06/17/resiliene4j-circuitbreaker-ringbitbuffer/

additional to documentation of Resilience4j - https://github.com/resilience4j/resilience4j/issues/966#issuecomment-618199350