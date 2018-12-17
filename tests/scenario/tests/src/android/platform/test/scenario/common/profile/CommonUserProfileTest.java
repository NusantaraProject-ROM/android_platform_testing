package android.platform.test.scenario.common.profile;

import android.platform.test.longevity.LongevitySuite;
import android.platform.test.rule.NaturalOrientationRule;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(LongevitySuite.class)
@SuiteClasses({
    // Referenced package names to avoid overlap.
    android.platform.test.scenario.common.sleep.Idle.class,
})

public class CommonUserProfileTest {
    // Class-level rules
    @ClassRule
    public static NaturalOrientationRule orientationRule = new NaturalOrientationRule();
}
