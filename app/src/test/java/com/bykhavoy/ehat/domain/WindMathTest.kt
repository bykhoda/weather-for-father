package com.bykhavoy.ehat.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WindMathTest {

    private val v = 10.0
    private val eps = 1e-6

    @Test fun headwind_from_north_bearing_north_is_full_headwind() {
        // wind FROM north (0°), heading north (0°) -> straight into your face.
        assertEquals(+v, WindMath.headwindComponent(v, 0.0, 0.0), eps)
    }

    @Test fun headwind_all_eight_rhumbs_bearing_zero() {
        assertEquals(+v, WindMath.headwindComponent(v, 0.0, 0.0), eps)     // N
        assertEquals(0.0, WindMath.headwindComponent(v, 90.0, 0.0), eps)   // E
        assertEquals(-v, WindMath.headwindComponent(v, 180.0, 0.0), eps)   // S -> tailwind
        assertEquals(0.0, WindMath.headwindComponent(v, 270.0, 0.0), eps)  // W
        assertEquals(+0.70710678 * v, WindMath.headwindComponent(v, 45.0, 0.0), 1e-5)  // NE
    }

    @Test fun crosswind_sign_left_vs_right() {
        // Wind FROM the east relative to a northbound route pushes to one side...
        assertTrue(WindMath.crosswindComponent(v, 90.0, 0.0) > 0)
        // ...and from the west to the other.
        assertTrue(WindMath.crosswindComponent(v, 270.0, 0.0) < 0)
    }

    @Test fun headwind_wraps_across_360() {
        // wind FROM 350°, heading 10° -> delta 340° ~ nearly head-on.
        val h = WindMath.headwindComponent(v, 350.0, 10.0)
        assertTrue(h > 0.9 * v, "expected near-full headwind, got $h")
    }

    @Test fun range_penalty_zero_headwind_is_exactly_zero() {
        assertEquals(0.0, WindMath.rangePenaltyPercent(27.0, 0.0), 0.0)
    }

    @Test fun range_penalty_zero_car_speed_no_div_by_zero() {
        assertEquals(0.0, WindMath.rangePenaltyPercent(0.0, 10.0), 0.0)
    }

    @Test fun range_penalty_positive_for_headwind() {
        assertTrue(WindMath.rangePenaltyPercent(27.0, 10.0) > 0.0)
    }

    @Test fun shortest_delta_takes_short_arc() {
        assertEquals(20.0, WindMath.shortestDelta(350.0, 10.0), eps)   // +20, not -340
        assertEquals(-20.0, WindMath.shortestDelta(10.0, 350.0), eps)  // -20, not +340
    }
}
