package tla.model

import tla.model.regard.RegardState

data class TLAState(
    val regard: RegardState = RegardState.default()
) {

}
