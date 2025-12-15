import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class McMetroTest {
    @Test
    void McMetroCorrectnessCheck() {
        long n = Arrays.stream(McMetro.class.getDeclaredMethods())
                .filter(x -> !Modifier.isPrivate(x.getModifiers()))
                .count();

        assertEquals(6, n);
    }

    @Test
    void DisjointSetCorrectnessCheck() {
        Method[] methods = NaiveDisjointSet.class.getDeclaredMethods();
        for (Method m : methods) {
            if (m.getModifiers() == 0) {
                switch (m.getName()) {
                    case "add" -> {
                        assertEquals(void.class, m.getReturnType());
                        assertEquals(Object.class, m.getParameterTypes()[0]);
                        assertEquals(1, m.getParameterTypes().length);
                    }
                    case "find" -> {
                        assertEquals(Object.class, m.getReturnType());
                        assertEquals(Object.class, m.getParameterTypes()[0]);
                        assertEquals(1, m.getParameterTypes().length);
                    }
                    case "union" -> {
                        assertEquals(void.class, m.getReturnType());
                        assertEquals(Object.class, m.getParameterTypes()[0]);
                        assertEquals(Object.class, m.getParameterTypes()[1]);
                        assertEquals(2, m.getParameterTypes().length);
                    }
                    default -> fail();
                }
            }
        }
    }

    @Test
    void maxPassengers() {
        McMetro mcMetro = TestUtils.maxPassengersBuilder("[1, 2, 100]");
        int mp = mcMetro.maxPassengers(new BuildingID(1), new BuildingID(2));
        int actual = 100;
        assertEquals(actual, mp);
    }


    @Test
    void bestMetroSystem() {
        McMetro mcMetro = TestUtils.bestMetroBuilder("[1, 2, 10]", 123);
        TrackID[] bms = mcMetro.bestMetroSystem();
        int[] actual = {0};
        TestUtils.trackIdsEqual(actual, bms);
    }

    @Test
    void searchForPassengers() {
        String[] passengers = {"Alex", "Bob", "Ally"};
        String[] expected = {"Alex", "Ally"};
        TestUtils.checkPassengerSearch(passengers, expected, "Al");
    }


    @Test
    void hireTicketCheckers() {
        TestUtils.testHiring("[1,2][2,3][3,4][1,3]", 3);
    }
}
