
package com.ngong.librasoftware.utils;

import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class AnimationUtils {
    public static void applyFadeIn(Node node, double durationMillis) {
        FadeTransition fade = new FadeTransition(Duration.millis(durationMillis), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }
}
