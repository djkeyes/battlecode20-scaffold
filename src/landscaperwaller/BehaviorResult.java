package landscaperwaller;

enum BehaviorResult {
    // the action complete successfully
    SUCCESS,
    // the action could not be peformed because preconditions of the action were not met
    FAIL,
    // the action could not be performed, but it might be possible next turn, so subsequent actions should be
    // canceled
    POSTPONED
}
