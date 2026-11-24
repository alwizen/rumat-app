package com.solu8i.cmms

import com.solu8i.cmms.ui.navigation.canExecuteHousekeeping
import com.solu8i.cmms.ui.navigation.isHousekeepingRole
import com.solu8i.cmms.ui.navigation.isHousekeepingSupervisor
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pembagian peran housekeeping:
 * - `patra-jasa` mengeksekusi checklist.
 * - `spv_gos` membuat plan dan menyetujui laporan harian.
 */
class HousekeepingRoleTest {

    @Test
    fun petugasCanExecuteButIsNotSupervisor() {
        assertTrue(canExecuteHousekeeping("patra-jasa"))
        assertFalse(isHousekeepingSupervisor("patra-jasa"))
        assertTrue(isHousekeepingRole("patra-jasa"))
    }

    @Test
    fun spvGosCannotExecuteChecklist() {
        assertTrue(isHousekeepingSupervisor("spv_gos"))
        assertFalse(canExecuteHousekeeping("spv_gos"))
        assertTrue(isHousekeepingRole("spv_gos"))
    }

    @Test
    fun handlesCommaSeparatedRoles() {
        assertTrue(canExecuteHousekeeping("ltsa_tad, patra-jasa"))
        assertFalse(canExecuteHousekeeping("ltsa_tad, spv_gos"))
        assertTrue(isHousekeepingSupervisor("spv_gos,ltsa_tad"))
    }

    @Test
    fun unrelatedRolesHaveNoHousekeepingAccess() {
        assertFalse(canExecuteHousekeeping("ltsa_tad"))
        assertFalse(isHousekeepingSupervisor("ltsa_tad"))
        assertFalse(isHousekeepingRole("ltsa_tad"))
        assertFalse(canExecuteHousekeeping(null))
        assertFalse(isHousekeepingRole(null))
    }
}
