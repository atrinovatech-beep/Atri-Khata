package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.StaffMemberEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueBright
import com.example.ui.theme.SkyBlueCardBg
import com.example.ui.theme.SkyBlueCardBorder
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite

/**
 * Predefined Roles with default privilege templates:
 * Admin, Partner, Manager, Accountant, Sales Person, Stock Manager, Entry Person
 */
val PREDEFINED_ROLES = listOf(
    "Admin",
    "Partner",
    "Manager",
    "Accountant",
    "Sales Person",
    "Stock Manager",
    "Entry Person"
)

fun getRoleDescription(role: String): String = when (role) {
    "Admin" -> "Full unrestricted access to all modules, settings & cash drawer"
    "Partner" -> "Co-owner access with view permissions to all financials"
    "Manager" -> "Operations, sales & purchase management with approval powers"
    "Accountant" -> "Vouchers, party ledgers, tax filing & balance sheets"
    "Sales Person" -> "Counter billing, sales invoices, POS & customer statements"
    "Stock Manager" -> "Stock entries, inventory adjustments & supplier orders"
    "Entry Person" -> "Restricted entry clerk with create-only permissions"
    else -> "Custom role"
}

@Composable
fun AdvancedStaffManagementSheet(
    viewModel: MainViewModel,
    onClose: () -> Unit
) {
    val staffMembers by viewModel.allStaff.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Screen state: LIST, ADD_EDIT, PERMISSION_CONFIG
    var activeView by remember { mutableStateOf<StaffViewMode>(StaffViewMode.List) }
    var selectedStaffForEdit by remember { mutableStateOf<StaffMemberEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var roleFilter by remember { mutableStateOf("All") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(top = 8.dp)
            .testTag("advanced_staff_management_sheet")
    ) {
        when (val mode = activeView) {
            is StaffViewMode.List -> {
                StaffListView(
                    staffMembers = staffMembers,
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    roleFilter = roleFilter,
                    onRoleFilterChange = { roleFilter = it },
                    onAddNewStaff = {
                        selectedStaffForEdit = null
                        activeView = StaffViewMode.AddEdit
                    },
                    onEditPermissions = { staff ->
                        selectedStaffForEdit = staff
                        activeView = StaffViewMode.Permissions(staff)
                    },
                    onDeleteStaff = { staff ->
                        viewModel.deleteStaffMember(staff)
                        Toast.makeText(context, "Staff ${staff.name} removed", Toast.LENGTH_SHORT).show()
                    },
                    onClose = onClose
                )
            }

            is StaffViewMode.AddEdit -> {
                AddEditStaffView(
                    existingStaff = selectedStaffForEdit,
                    onSave = { staffData ->
                        if (selectedStaffForEdit != null) {
                            viewModel.updateStaffMember(staffData)
                            Toast.makeText(context, "Staff details updated", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addStaffMember(
                                name = staffData.name,
                                phone = staffData.phone,
                                role = staffData.role,
                                pin = staffData.pin,
                                avatarColor = staffData.avatarColor,
                                canCreateSalesInvoice = staffData.canCreateSalesInvoice,
                                canEditSalesInvoice = staffData.canEditSalesInvoice,
                                canDeleteSalesInvoice = staffData.canDeleteSalesInvoice,
                                canViewCustomerLedger = staffData.canViewCustomerLedger,
                                canGiveDiscounts = staffData.canGiveDiscounts,
                                canChangeSellingPrice = staffData.canChangeSellingPrice,
                                canCreatePurchaseEntry = staffData.canCreatePurchaseEntry,
                                canEditPurchaseEntry = staffData.canEditPurchaseEntry,
                                canDeletePurchaseEntry = staffData.canDeletePurchaseEntry,
                                canViewSupplierLedger = staffData.canViewSupplierLedger,
                                canAccessCashDrawer = staffData.canAccessCashDrawer,
                                canViewFinancialReports = staffData.canViewFinancialReports,
                                canManageInventory = staffData.canManageInventory
                            )
                            Toast.makeText(context, "New staff invited successfully", Toast.LENGTH_SHORT).show()
                        }
                        activeView = StaffViewMode.List
                    },
                    onConfigurePermissions = { staffData ->
                        selectedStaffForEdit = staffData
                        activeView = StaffViewMode.Permissions(staffData)
                    },
                    onBack = { activeView = StaffViewMode.List }
                )
            }

            is StaffViewMode.Permissions -> {
                GranularPermissionsView(
                    staff = mode.staff,
                    onSavePermissions = { updatedStaff ->
                        viewModel.updateStaffMember(updatedStaff)
                        Toast.makeText(context, "Permissions saved for ${updatedStaff.name}", Toast.LENGTH_SHORT).show()
                        activeView = StaffViewMode.List
                    },
                    onBack = { activeView = StaffViewMode.List }
                )
            }
        }
    }
}

sealed class StaffViewMode {
    object List : StaffViewMode()
    object AddEdit : StaffViewMode()
    data class Permissions(val staff: StaffMemberEntity) : StaffViewMode()
}

@Composable
fun StaffListView(
    staffMembers: List<StaffMemberEntity>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    roleFilter: String,
    onRoleFilterChange: (String) -> Unit,
    onAddNewStaff: () -> Unit,
    onEditPermissions: (StaffMemberEntity) -> Unit,
    onDeleteStaff: (StaffMemberEntity) -> Unit,
    onClose: () -> Unit
) {
    val filteredStaff = staffMembers.filter {
        (roleFilter == "All" || it.role.equals(roleFilter, ignoreCase = true)) &&
                (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SkyBlueCardBg)
                            .border(1.dp, SkyBlueBright.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Group,
                            contentDescription = null,
                            tint = SkyBlueBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Staff & Access Control",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
                Text(
                    text = "Role-based security & granular permissions",
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(start = 42.dp)
                )
            }

            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
            }
        }

        HorizontalDivider(color = CardBorder, thickness = 1.dp)

        // Search and Filter Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search by staff name or mobile...", color = TextSubtle, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = SkyBlueBright, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = SkyBlueBright,
                    unfocusedBorderColor = CardBorder,
                    focusedContainerColor = CardDark,
                    unfocusedContainerColor = CardDark
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter chips by Role
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Admin", "Manager", "Sales Person", "Accountant").forEach { role ->
                    val isSelected = roleFilter == role
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) SkyBlue else CardDark)
                            .border(1.dp, if (isSelected) SkyBlueBright else CardBorder, RoundedCornerShape(20.dp))
                            .clickable { onRoleFilterChange(role) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = role,
                            color = if (isSelected) Color.Black else TextGrayLight,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Staff List Cards
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredStaff, key = { it.id }) { staff ->
                StaffMemberCard(
                    staff = staff,
                    onEditPermissions = { onEditPermissions(staff) },
                    onDelete = { onDeleteStaff(staff) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Bottom Bar: + Invite New Staff Member (Pill button)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .border(1.dp, CardBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(16.dp)
        ) {
            Button(
                onClick = onAddNewStaff,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SkyBlue,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("add_new_staff_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add / Invite Staff Member",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun StaffMemberCard(
    staff: StaffMemberEntity,
    onEditPermissions: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with Initials
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(staff.avatarColor).copy(alpha = 0.2f))
                        .border(1.5.dp, Color(staff.avatarColor), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = staff.name.split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .take(2)
                        .joinToString("")
                    Text(
                        text = if (initials.isNotBlank()) initials else "ST",
                        color = SkyBlueBright,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = staff.name,
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Role Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SkyBlueCardBg)
                                .border(1.dp, SkyBlueBright.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = staff.role,
                                color = SkyBlueBright,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            tint = TextSubtle,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (staff.phone.startsWith("+977")) staff.phone else "+977 ${staff.phone}",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = TextSubtle,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PIN: ••••",
                            color = TextSubtle,
                            fontSize = 11.sp
                        )
                    }
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextMuted)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(SurfaceDark)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Role & Permissions", color = TextWhite) },
                            onClick = {
                                menuExpanded = false
                                onEditPermissions()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = SkyBlueBright)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Staff", color = Color(0xFFFF5252)) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252))
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Permissions preview summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PermissionBadge(
                        label = "Sales",
                        enabled = staff.canCreateSalesInvoice,
                        icon = Icons.Outlined.Receipt
                    )
                    PermissionBadge(
                        label = "Purchases",
                        enabled = staff.canCreatePurchaseEntry,
                        icon = Icons.Outlined.ShoppingCart
                    )
                    PermissionBadge(
                        label = "Drawer",
                        enabled = staff.canAccessCashDrawer,
                        icon = Icons.Outlined.Lock
                    )
                }

                // Edit Permissions Pill Button
                OutlinedButton(
                    onClick = onEditPermissions,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SkyBlueBright
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(SkyBlueBright.copy(alpha = 0.5f))
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Permissions", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun PermissionBadge(
    label: String,
    enabled: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) SkyBlueBright else TextSubtle,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (enabled) TextWhite else TextSubtle,
            fontWeight = if (enabled) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
fun AddEditStaffView(
    existingStaff: StaffMemberEntity?,
    onSave: (StaffMemberEntity) -> Unit,
    onConfigurePermissions: (StaffMemberEntity) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(existingStaff?.name ?: "") }
    var phone by remember { mutableStateOf(existingStaff?.phone ?: "") }
    var role by remember { mutableStateOf(existingStaff?.role ?: "Sales Person") }
    var pin by remember { mutableStateOf(existingStaff?.pin ?: "1234") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (existingStaff == null) "Add Staff Member" else "Edit Staff Details",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        }

        HorizontalDivider(color = CardBorder)
        Spacer(modifier = Modifier.height(16.dp))

        // Input Fields
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                errorMessage = ""
            },
            label = { Text("Full Name *") },
            placeholder = { Text("e.g. Ramesh Shrestha") },
            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = SkyBlueBright) },
            colors = staffTextFieldColors(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = {
                phone = it
                errorMessage = ""
            },
            label = { Text("Mobile Number (Nepal +977) *") },
            placeholder = { Text("98XXXXXXXX") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = SkyBlueBright) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = staffTextFieldColors(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = { if (it.length <= 4) pin = it },
            label = { Text("4-Digit Security PIN *") },
            placeholder = { Text("1234") },
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = SkyBlueBright) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            colors = staffTextFieldColors(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Select Role-Based Access Level:",
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            text = "Permissions are auto-configured based on selected role",
            color = TextSubtle,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(PREDEFINED_ROLES) { r ->
                val isSelected = role == r
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) SkyBlueCardBg else CardDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isSelected) SkyBlueBright else CardBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { role = r }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { role = r },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = SkyBlueBright,
                                unselectedColor = TextSubtle
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = r,
                                color = if (isSelected) SkyBlueBright else TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = getRoleDescription(r),
                                color = TextSubtle,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color(0xFFFF5252),
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Save Button (Pill shaped)
        Button(
            onClick = {
                if (name.isBlank() || phone.isBlank()) {
                    errorMessage = "Please enter staff name and mobile number"
                    return@Button
                }
                val staffToSave = (existingStaff ?: StaffMemberEntity(
                    name = name,
                    phone = phone,
                    role = role,
                    pin = pin
                )).copy(
                    name = name.trim(),
                    phone = phone.trim(),
                    role = role,
                    pin = pin.ifBlank { "1234" }
                )
                onSave(staffToSave)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = SkyBlue,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = if (existingStaff == null) "Save & Continue" else "Update Staff Member",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun GranularPermissionsView(
    staff: StaffMemberEntity,
    onSavePermissions: (StaffMemberEntity) -> Unit,
    onBack: () -> Unit
) {
    // Granular states
    var canCreateSalesInvoice by remember { mutableStateOf(staff.canCreateSalesInvoice) }
    var canEditSalesInvoice by remember { mutableStateOf(staff.canEditSalesInvoice) }
    var canDeleteSalesInvoice by remember { mutableStateOf(staff.canDeleteSalesInvoice) }
    var canViewCustomerLedger by remember { mutableStateOf(staff.canViewCustomerLedger) }
    var canGiveDiscounts by remember { mutableStateOf(staff.canGiveDiscounts) }
    var canChangeSellingPrice by remember { mutableStateOf(staff.canChangeSellingPrice) }

    var canCreatePurchaseEntry by remember { mutableStateOf(staff.canCreatePurchaseEntry) }
    var canEditPurchaseEntry by remember { mutableStateOf(staff.canEditPurchaseEntry) }
    var canDeletePurchaseEntry by remember { mutableStateOf(staff.canDeletePurchaseEntry) }
    var canViewSupplierLedger by remember { mutableStateOf(staff.canViewSupplierLedger) }

    var canAccessCashDrawer by remember { mutableStateOf(staff.canAccessCashDrawer) }
    var canViewFinancialReports by remember { mutableStateOf(staff.canViewFinancialReports) }
    var canManageInventory by remember { mutableStateOf(staff.canManageInventory) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("granular_permissions_view")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Granular Permissions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = "${staff.name} • ${staff.role}",
                    fontSize = 12.sp,
                    color = SkyBlueBright
                )
            }
        }

        HorizontalDivider(color = CardBorder)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(top = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Group 1: Sales Vouchers Permissions
            item {
                PermissionGroupCard(
                    title = "Sales Vouchers & Billing",
                    subtitle = "Permissions related to invoices, cash memo & customer accounts",
                    icon = Icons.Outlined.Receipt
                ) {
                    PermissionToggleRow(
                        title = "Create Sales Invoices & POS",
                        subtitle = "Generate invoices, estimate bills & cash receipts",
                        checked = canCreateSalesInvoice,
                        onCheckedChange = { canCreateSalesInvoice = it }
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    PermissionToggleRow(
                        title = "Edit / Modify Sales Invoices",
                        subtitle = "Edit existing items, quantities and taxes",
                        checked = canEditSalesInvoice,
                        onCheckedChange = { canEditSalesInvoice = it }
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    PermissionToggleRow(
                        title = "Delete Sales Invoices",
                        subtitle = "Cancel and void previously settled sales",
                        checked = canDeleteSalesInvoice,
                        onCheckedChange = { canDeleteSalesInvoice = it }
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    PermissionToggleRow(
                        title = "View Customer Outstanding & Ledger",
                        subtitle = "Check customer credit limit, history & statements",
                        checked = canViewCustomerLedger,
                        onCheckedChange = { canViewCustomerLedger = it }
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    PermissionToggleRow(
                        title = "Apply Custom Discounts",
                        subtitle = "Permit manual discount percentage during billing",
                        checked = canGiveDiscounts,
                        onCheckedChange = { canGiveDiscounts = it }
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    PermissionToggleRow(
                        title = "Override Item Selling Price",
                        subtitle = "Allow changing the unit price at checkout",
                        checked = canChangeSellingPrice,
                        onCheckedChange = { canChangeSellingPrice = it }
                    )
                }
            }

            // Group 2: Purchase Vouchers Permissions
            item {
                PermissionGroupCard(
                    title = "Purchase Vouchers & Inward Stock",
                    subtitle = "Permissions related to supplier bills & stock acquisition",
                    icon = Icons.Outlined.ShoppingCart
                ) {
                    PermissionToggleRow(
                        title = "Create Purchase Entries",
                        subtitle = "Record new purchase bills and inbound goods",
                        checked = canCreatePurchaseEntry,
                        onCheckedChange = { canCreatePurchaseEntry = it }
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    PermissionToggleRow(
                        title = "Edit Purchase Invoices",
                        subtitle = "Modify purchase quantities, rates & lot numbers",
                        checked = canEditPurchaseEntry,
                        onCheckedChange = { canEditPurchaseEntry = it }
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    PermissionToggleRow(
                        title = "Delete Purchase Records",
                        subtitle = "Remove historical purchase transactions",
                        checked = canDeletePurchaseEntry,
                        onCheckedChange = { canDeletePurchaseEntry = it }
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    PermissionToggleRow(
                        title = "View Supplier Ledger & Payables",
                        subtitle = "Access vendor credit balances and payment records",
                        checked = canViewSupplierLedger,
                        onCheckedChange = { canViewSupplierLedger = it }
                    )
                }
            }

            // Group 3: Financial & Cash Control
            item {
                PermissionGroupCard(
                    title = "Financial Control & Inventory",
                    subtitle = "Cash drawer access, inventory updates & business reports",
                    icon = Icons.Outlined.Lock
                ) {
                    PermissionToggleRow(
                        title = "Access Physical Cash Drawer",
                        subtitle = "Open RJ11 drawer and perform cash payouts",
                        checked = canAccessCashDrawer,
                        onCheckedChange = { canAccessCashDrawer = it }
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    PermissionToggleRow(
                        title = "View Financial Reports & Daybook",
                        subtitle = "Access P&L summaries, balance sheets & taxes",
                        checked = canViewFinancialReports,
                        onCheckedChange = { canViewFinancialReports = it }
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    PermissionToggleRow(
                        title = "Manage Inventory Catalog",
                        subtitle = "Add items, update stock alerts & change purchase prices",
                        checked = canManageInventory,
                        onCheckedChange = { canManageInventory = it }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Save Permissions Button
        Button(
            onClick = {
                val updated = staff.copy(
                    canCreateSalesInvoice = canCreateSalesInvoice,
                    canEditSalesInvoice = canEditSalesInvoice,
                    canDeleteSalesInvoice = canDeleteSalesInvoice,
                    canViewCustomerLedger = canViewCustomerLedger,
                    canGiveDiscounts = canGiveDiscounts,
                    canChangeSellingPrice = canChangeSellingPrice,
                    canCreatePurchaseEntry = canCreatePurchaseEntry,
                    canEditPurchaseEntry = canEditPurchaseEntry,
                    canDeletePurchaseEntry = canDeletePurchaseEntry,
                    canViewSupplierLedger = canViewSupplierLedger,
                    canAccessCashDrawer = canAccessCashDrawer,
                    canViewFinancialReports = canViewFinancialReports,
                    canManageInventory = canManageInventory
                )
                onSavePermissions(updated)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = SkyBlue,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(bottom = 8.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Permissions", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
fun PermissionGroupCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(SkyBlueCardBg)
                        .border(1.dp, SkyBlueBright.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = SkyBlueBright, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(subtitle, color = TextSubtle, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = CardBorder)

            content()
        }
    }
}

@Composable
fun PermissionToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (checked) TextWhite else TextMuted,
                fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 14.sp
            )
            Text(
                text = subtitle,
                color = TextSubtle,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Custom Corporate Blue Toggle Switch
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SkyBlue,
                uncheckedThumbColor = TextSubtle,
                uncheckedTrackColor = BackgroundDark,
                uncheckedBorderColor = CardBorder
            )
        )
    }
}

@Composable
fun staffTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextWhite,
    unfocusedTextColor = TextWhite,
    focusedBorderColor = SkyBlueBright,
    unfocusedBorderColor = CardBorder,
    focusedLabelColor = SkyBlueBright,
    unfocusedLabelColor = TextMuted,
    focusedContainerColor = CardDark,
    unfocusedContainerColor = CardDark,
    cursorColor = SkyBlueBright
)
