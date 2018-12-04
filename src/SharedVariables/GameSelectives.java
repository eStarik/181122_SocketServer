package SharedVariables;

import java.util.Random;
import static SharedVariables.constants.*;

public class GameSelectives {

    public static int randTime(){
        int time;
        Random random = new Random();
        time = random.nextInt(MAX_TIME_VALUE-MIN_TIME_VALUE) + MIN_TIME_VALUE;
        return time;
    }

    public static int randButtonCount(){
        int buttonCount;
        Random random = new Random();
        buttonCount = random.nextInt(MAX_BUTTONS-MIN_BUTTONS) + MIN_BUTTONS;
        return buttonCount;
    }

    public static int getButton(){
        int button;
        Random random = new Random();
        button = random.nextInt(BUTTON_COUNT);
        return button;
    }
}
