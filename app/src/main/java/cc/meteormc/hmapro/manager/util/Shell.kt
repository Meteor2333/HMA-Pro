package cc.meteormc.hmapro.manager.util

import cc.meteormc.xposedkit.XLog
import com.topjohnwu.superuser.Shell as CoreShell

object Shell {
    private const val TAG = "Shell"

    fun exec(
        cmd: String,
        root: Boolean = false,
        result: (Result<List<String>>) -> Unit
    ) = CoreShell.getShell { shell ->
        if (root && !shell.isRoot) {
            result.invoke(Result.failure(IllegalStateException("Permission denied")))
            return@getShell
        }

        shell.newJob().add(cmd).submit {
            if (it.isSuccess) {
                result.invoke(Result.success(it.out))
            } else {
                result.invoke(Result.failure(RuntimeException("Command execution failed")))
                XLog.w(TAG, "Failed to execute command! exit code: ${it.code} err: ${it.err.joinToString()}")
            }
        }
    }
}