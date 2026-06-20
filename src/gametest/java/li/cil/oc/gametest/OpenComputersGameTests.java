package li.cil.oc.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * Placeholder GameTests for OpenComputers-CE Test Lab.
 * Expand with computer placement and cable connection tests.
 */
@PrefixGameTestTemplate(false)
@GameTestHolder("opencomputers")
public class OpenComputersGameTests {

    @GameTest(template = "empty1x1")
    public static void placeholderBoot(final GameTestHelper helper) {
        // P0 future: verify OC blocks register in test world
        helper.succeed();
    }
}
