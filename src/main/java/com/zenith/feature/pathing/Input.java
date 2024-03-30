package com.zenith.feature.pathing;

import lombok.Data;

@Data
public class Input {
    public float movementSideways;
    public float movementForward;
    public boolean pressingForward;
    public boolean pressingBack;
    public boolean pressingLeft;
    public boolean pressingRight;
    public boolean jumping;
    public boolean sneaking;
    public boolean sprinting;
    public boolean leftClick;
    public boolean rightClick;

    public Input(final boolean pressingForward,
                 final boolean pressingBack,
                 final boolean pressingLeft,
                 final boolean pressingRight,
                 final boolean jumping,
                 final boolean sneaking,
                 final boolean sprinting,
                 final boolean leftClick,
                 final boolean rightClick
    ) {
        this.pressingForward = pressingForward;
        this.pressingBack = pressingBack;
        this.pressingLeft = pressingLeft;
        this.pressingRight = pressingRight;
        this.jumping = jumping;
        this.sneaking = sneaking;
        this.sprinting = sprinting;
        this.leftClick = leftClick;
        this.rightClick = rightClick;
    }

    public Input() {
        this(false, false, false, false, false, false, false, false, false);
    }

    public void reset() {
        movementSideways = 0;
        movementForward = 0;
        pressingForward = false;
        pressingBack = false;
        pressingLeft = false;
        pressingRight = false;
        jumping = false;
        sneaking = false;
        sprinting = false;
        leftClick = false;
        rightClick = false;
    }
}
