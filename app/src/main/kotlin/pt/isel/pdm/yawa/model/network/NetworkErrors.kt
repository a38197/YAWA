package pt.isel.pdm.yawa.model.network

import android.content.Context
import com.android.volley.*
import pt.isel.pdm.yawa.R


class NetworkErrors {

    companion object {
        val NOT_FOUND = "502"

        fun getNetworkError(context: Context, e: Throwable): String{
            when(e.cause){
                is AuthFailureError -> return context.getString(R.string.auth_failure_error)
                is NetworkError -> return context.getString(R.string.network_error)
                is ParseError -> return context.getString(R.string.parse_error)
                is TimeoutError -> return context.getString(R.string.timeout_error)
                is ServerError -> {
                    if(isNotFound(e.cause as ServerError))
                        return context.getString(R.string.city_not_found)
                    else
                        return context.getString(R.string.server_error)
                }
            }

            return ""
        }

        private fun isNotFound(error: ServerError): Boolean{
            var data = "";
            for (byte in error.networkResponse.data){
                if(byte.toChar().equals('\n'))
                    break;
                data += byte.toChar()
            }

            return NOT_FOUND.equals(data);
        }
    }
}