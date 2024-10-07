package com.example.fickbookauthorhelper.logic.workers

/*@HiltWorker
class ProxyWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParameters: WorkerParameters,
    private val proxyManager: IProxyManager,
    private val client: OkHttpClient,
    private val timeout: Long
) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val result = proxyManager.findAndSaveWorkingProxy(client, timeout)
                if (result != null) {
                    println("ProxyWorker success")
                    Result.success()
                } else {
                    println("ProxyWorker proxy was not found")
                    Result.retry()
                }
            } catch (e: Exception) {
                println("ProxyWorker failed: ${e.cause}")
                Result.failure()
            }
        }
    }
}*/
