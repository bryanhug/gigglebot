package scripts;

import org.tribot.script.Script;
import org.tribot.api2007.*;
import org.tribot.api.*;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.types.*;
import org.tribot.script.ScriptManifest;


@ScriptManifest(authors = { "gigglez" }, category = "Professions", name = "Basic Cannon Ball Maker", version = 1.00, description = "Makes cannonballs from steel bars.", gameMode = 1)

public class CannonBall extends Script {

    private boolean done =  false;


    private boolean isAtSmith() {
        final RSObject[] furnaces = Objects.findNearest(100, "Furnace");

        if (furnaces.length < 1){
            return false;
        }

        return furnaces[0].isOnScreen();
    }//isAtSmith

    private boolean isInBank() {
        final RSObject[] booths = Objects.findNearest(20, "Bank booth");
        if (booths.length > 1) {
            if (booths[0].isOnScreen())
                return true;

        }
        final RSNPC[] bankers = NPCs.findNearest("Banker");
        if (bankers.length < 1)
            return false;

        return bankers[0].isOnScreen();
    }//isInBank

    private boolean smith() {
        println("smith");

        //gets rid of level up menu
        if (NPCChat.getClickContinueInterface() != null)
            NPCChat.clickContinue(false);

        if (isSmithing()) {
            final long timeout = System.currentTimeMillis() + General.random(60000, 90000);
            while (!doneSmithing() && System.currentTimeMillis() < timeout) {
                sleep(100, 150);
            }//while
        }//isSmithing

        final RSObject[] furnaces = Objects.findNearest(100, "Furnace");

        if (furnaces.length < 1)
            return false;
        if (!furnaces[0].isOnScreen()) {
            if (!Walking.walkPath(Walking.generateStraightPath(furnaces[0])))
                return false;

            if (!Timing.waitCondition(new Condition() {
                @Override
                public boolean active() {
                    General.sleep(100); // Sleep to reduce CPU usage.
                    return furnaces[0].isOnScreen();
                }
                }, General.random(8000, 9300)))
                return false;
        }

        if (!DynamicClicking.clickRSObject(furnaces[0], "Smelt furnace"))
            return false;

        //162 is found from tools > interface explorer
//        RSInterface allButton = Interfaces.get(270,12);
        RSInterface makeButton = Interfaces.get(270,14);

//        if (allButton != null)
//            allButton.click();
        if (makeButton != null){
            makeButton.click();
            makeButton.click();
        }


        Timing.waitCondition(new Condition() {
            @Override
            public boolean active() {
                return doneSmithing();
            }
        }, General.random(10000, 12000));


        return true;
    }//smith

    private boolean isSmithing() {
        return Player.getAnimation() > 0;
    }

    private boolean doneSmithing(){
        // replacement for Inventory.isFull
        return Inventory.find("Steel bar").length == 0;
    }

    private boolean walkToBank() {
        println("walkToBank");
        if (!WebWalking.walkToBank()) { // We failed to walk to the bank. Let's return false.
            return false;
        }

        return Timing.waitCondition(new Condition() {
            @Override public boolean active() {
                General.sleep(200, 300); // Reduces CPU usage.
                return isInBank();
            }
        }, General.random(8000, 9000));
    }

    private boolean walkToSmith() {
        println("walkToSmith");
        final RSObject[] furnaces = Objects.findNearest(100, "Furnace");

        if (furnaces.length < 1)
            return false;

        if (!WebWalking.walkTo(furnaces[0]))
            return false;

        return Timing.waitCondition(new Condition() { // If we reach the trees before the timeout, this method will return
            // true. Otherwise, it will return false.
            @Override public boolean active() {
                General.sleep(200, 300); // Reduces CPU usage.
                return isAtSmith();
            }
        }, General.random(8000, 9000));
    }//walkToSmith

    private boolean bank() {
        println("bank");
        if (!Banking.isBankScreenOpen()) { // The bank screen is not open. Let's open it.
            if (!Banking.openBank())
                return false;
        }

        if (Inventory.find("Cannonball").length > 0)
            if (Banking.depositAllExcept("Ammo mould") < 0)
                return false;

        final RSItem[] steelBars = Banking.find("Steel bar");
        if (steelBars.length > 0){
            if (Banking.withdraw(0,"Steel bar"))//withdraw all
                return false;
        }
        else
            done = true;

        return Timing.waitCondition(new Condition() {
            @Override
            public boolean active() {
                return !Inventory.isFull();
            }
        }, General.random(3000, 4000));
    }//bank

    @Override
    public void run() {
        // TODO: Add functionality for Anti-Ban Compliance
        while (!done) {

            sleep(50);

            if (isAtSmith()) {
                if (doneSmithing())
                    walkToBank();
                else
                    smith();
            }
            else if (isInBank()) {
                if (doneSmithing())
                    bank();
                else
                    walkToSmith();
            }
            else {
                if (doneSmithing())
                    walkToBank();
                else
                    walkToSmith();
            }
        }//while
    }//run

}//CannonBall