package com.itsronald.twenty2020.settings

import com.itsronald.twenty2020.base.Activity
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener
import javax.inject.Inject

/**
 * A subclass of [SnackbarOnDeniedPermissionListener] that additionally implements
 * [onPermissionRationaleShouldBeShown]. Since it responds to a user action, it assumes that
 * the permission is not permanently denied so as to alert the user that the setting cannot be
 * set.
 */
@Activity
class SnackbarPermissionListener
        @Inject constructor(baseListener: SnackbarOnDeniedPermissionListener) :
        PermissionListener by baseListener {

    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest,
                                                    token: PermissionToken) {
        token.cancelPermissionRequest()

        val reconstructedRequest = PermissionRequest(permission.name)
        val permanentlyDenied = false
        onPermissionDenied(PermissionDeniedResponse(reconstructedRequest, permanentlyDenied))
    }
}
