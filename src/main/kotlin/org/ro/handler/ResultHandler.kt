package org.ro.handler

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.core.aggregator.NavigationAggregator
import org.ro.to.Result
import org.ro.to.TransferObject

class ResultHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        logEntry.aggregator = NavigationAggregator()
        update()
    }

    @UnstableDefault
    override fun parse(jsonStr: String): TransferObject? {
        return Json.parse(Result.serializer(), jsonStr)
    }

}
