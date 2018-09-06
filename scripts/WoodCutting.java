package scripts;
import org.tribot.script.Script;
import org.tribot.api2007.*;
import org.tribot.api2007.NPCChat;

import org.tribot.api.*;
import org.tribot.api.types.generic.Condition; 
import org.tribot.api2007.types.*;
import org.tribot.script.ScriptManifest;


@ScriptManifest(authors = { "gigglez" }, category = "Woodcutting", name = "Basic Area Tree Cutter", version = 1.00, description = "Cuts and banks trees in Draynor.", gameMode = 1)

public class WoodCutting extends Script {

    private RSTile last_tree_tile;

    private RSTile[] townSquare = new RSTile[]{
        new RSTile(3186,3468,0),
        new RSTile(3144,3468,0),
        new RSTile(3139,3473,0),
        new RSTile(3139,3481,0),
        new RSTile(3142,3484,0),
        new RSTile(3142,3491,0),
        new RSTile(3139,3495,0),
        new RSTile(3139,3512,0),
        new RSTile(3143,3516,0),
        new RSTile(3157,3516,0),
        new RSTile(3160,3513,0),
        new RSTile(3167,3513,0),
        new RSTile(3171,3516,0),
        new RSTile(3188,3516,0),
        new RSTile(3196,3507,0),
        new RSTile(3189,3497,0),
        new RSTile(3189,3479,0),
        new RSTile(3186,3476,0)
    };

    private RSArea farmZone = new RSArea(townSquare);

    private boolean isAtTrees() {
        // TODO: Change functionality to go to trees in an area
        final RSTile playerLoc = Player.getPosition();
        return farmZone.contains(playerLoc);
    }//isAtTrees

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

    private boolean cut() {
        if (isCutting()) {
            final long timeout = System.currentTimeMillis() + General.random(60000, 90000); 

            while (isCutting() && System.currentTimeMillis() < timeout) {
                sleep(100, 150); 

                if (this.last_tree_tile != null) {
                    if (!Objects.isAt(this.last_tree_tile, "Tree")) {
                        break;
                    }
                }
            }//while
        }//isCutting

        final RSObject[] trees = Objects.findNearest(100, "Tree");

        if (trees.length < 1)
            return false;
        if (!trees[0].isOnScreen()) {
            if (!Walking.walkPath(Walking.generateStraightPath(trees[0]))) 
                return false;

            if (!Timing.waitCondition(new Condition() { 	
                @Override
                public boolean active() {
                    General.sleep(100); // Sleep to reduce CPU usage.
                    return trees[0].isOnScreen();
                }
            }, General.random(8000, 9300)))	
            return false;
        }

        if (!DynamicClicking.clickRSObject(trees[0], "Chop down")) 
            return false;

        Timing.waitCondition(new Condition() {
            @Override
            public boolean active() {
                return !isCutting();
            }
        }, General.random(1000, 1200));

        //TODO: add functionality to get rid of level up menu
        // RSInterfaceChild LevelUp = getClickContinueInterface();
        // if (LevelUp != null)
        //     LevelUp.clickContinue(false); // click continue if we level up
        

        if (Timing.waitCondition(new Condition() {
            @Override
            public boolean active() {
                return isCutting();
            }
        }, General.random(8000, 9000))) {
            this.last_tree_tile = trees[0].getPosition().clone(); 	
            return true;
        }
        return false;
    }//cut

    private boolean isCutting() {
        return Player.getAnimation() > 0;		
    }

    private boolean walkToBank() {
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

    private boolean walkToTrees() {
        // TODO: Change functionality to walk to trees in the given are and chop there
        // TODO: Add functionality for being on the second level of bank
        final RSTile destination = farmZone.getRandomTile();			

        if (!WebWalking.walkTo(destination))
            return false;
		
        return Timing.waitCondition(new Condition() { // If we reach the trees before the timeout, this method will return	
                                                      // true. Otherwise, it will return false.	
            @Override public boolean active() {
                General.sleep(200, 300); // Reduces CPU usage.	
                return isAtTrees();
            }
        }, General.random(8000, 9000));
    }//walkToTrees

    private boolean bank() {
        if (!Banking.isBankScreenOpen()) { // The bank screen is not open. Let's open it.		
            if (!Banking.openBank())
                return false;		
        }
		
        final String[] axe_names = {
            "Bronze axe",
            "Iron Axe",
            "Black Axe",
            "Steel Axe",
            "Mithril Axe",
            "Adamant Axe",
            "Rune Axe",
            "Dragon Axe"
        };
        if (Inventory.find(axe_names).length > 0) { 	
            if (Banking.depositAllExcept(axe_names) < 1)
                return false;
        } 
        else {
            if (Banking.depositAll() < 1)
                return false;
        }		
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
        while (true) {

            sleep(50);
            
            if (isAtTrees()) {	
                if (Inventory.isFull()) { // The inventory is full	
                    walkToBank(); // Let's walk to the bank			
                } 
                else // The inventory is not full	
                    cut(); // Let's cut the willows.	
            }
            else if (isInBank()) { // We are at the bank		
                if (Inventory.isFull())      
                    bank();
                else
                    walkToTrees();
            } 
            else { 	
                if (Inventory.isFull())
                    walkToBank();		
                else
                    walkToTrees();
            }
        }//while 
    }//run
}//WoodCutting