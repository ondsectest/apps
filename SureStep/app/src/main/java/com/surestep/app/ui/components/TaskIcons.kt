package com.surestep.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LightbulbCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.Window
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Stable string keys map to icons so the database never stores a resource id
 * that could shift between builds.
 */
object TaskIcons {

    val all: List<Pair<String, ImageVector>> = listOf(
        "lock" to Icons.Filled.Lock,
        "keys" to Icons.Filled.VpnKey,
        "gas" to Icons.Filled.LocalFireDepartment,
        "lights" to Icons.Filled.LightbulbCircle,
        "power" to Icons.Filled.Power,
        "electric" to Icons.Filled.Bolt,
        "water" to Icons.Filled.Water,
        "window" to Icons.Filled.Window,
        "wallet" to Icons.Filled.AccountBalanceWallet,
        "phone" to Icons.Filled.PhoneAndroid,
        "laptop" to Icons.Filled.Laptop,
        "charger" to Icons.Filled.Power,
        "bag" to Icons.Filled.Backpack,
        "medicine" to Icons.Filled.Medication,
        "car" to Icons.Filled.DirectionsCar,
        "flight" to Icons.Filled.Flight,
        "shopping" to Icons.Filled.ShoppingBag,
        "pet" to Icons.Filled.Pets,
        "check" to Icons.Filled.CheckCircle,
    )

    private val byKey: Map<String, ImageVector> = all.toMap()

    fun forKey(key: String?): ImageVector = byKey[key] ?: Icons.Filled.CheckCircle

    val defaultKey: String = "check"
}
