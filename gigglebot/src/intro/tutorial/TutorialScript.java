package intro.tutorial;

import org.tribot.script.Script;
import org.tribot.api2007.*;
import org.tribot.api.*;
import org.tribot.api.types.generic.Condition; 
import org.tribot.api2007.types.*;
import org.tribot.script.ScriptManifest;


@ScriptManifest(authors = { "gigglez" }, category = "Woodcutting", name = "Basic Willow Cutter", version = 1.00, description = "Cuts and banks willows in Draynor.", gameMode = 1)

public class TutorialScript extends Script {

    private RSTile last_tree_tile;

    private boolean isAtTrees() {
        // We search for the trees by their name - Willow. The means that we	
        // will search for the trees within a radius of 20 tiles from our	
        // character. If they are more than 20 tiles away, the findNearest
        // method will not find them. Although, if they are more than 20 tiles
        // away, we can be sure that they aren't on the screen.	
        // We will now store the variable returned form findNearest in the	
        // variable called 'willows'. We declare this variable final because we	
        // will not be changing it's contents after the variable is set.
        // According to the API, the returned value from findNearest will be
        // RSObject[]. If there are no trees found, the array will not contain
        // any elements. The returned value cannot be null, and therefore we
        // don't have to null check it.		
        // Next, we check the length of the array. If the length is less than 1,	
        // we know that no trees were found. We can now return false.
        final RSObject[] willows = Objects.findNearest(20, "Willow");
        if (willows.length < 1)
            return false;
        // The array contains at least one element. The first element in the
        // array will be the nearest tree to our character. Let's check if this
        // tree is on screen. We will return the value.	
        return willows[0].isOnScreen();    
    }//isAtTrees

    private boolean isInBank() {
        // Let's first search for on-screen booths.	
        final RSObject[] booths = Objects.findNearest(20, "Bank booth");
        if (booths.length > 1) { // A booth is in the array. Let's check if the first element of the		
                            // array is on the screen.		
        if (booths[0].isOnScreen())
            return true;
            // The booth is on the screen. We don't need to
            // check for visible bankers since we already know	
            // that we are in the bank. Let's exit this method	
            // and return true.			
        }

        // Nope, the nearest booth is no visible. Let's go and and search	
        // for bankers.		
        final RSNPC[] bankers = NPCs.findNearest("Banker");
        if (bankers.length < 1)
            return false; // No booths are on the screen, and no bankers exist.	
                        // Let's just exit already since we know that we are		
                        // not in the bank. We will return false.	
                        // Okay, so we found a banker. The first element in the array is the	
                        // nearest NPC. Let's check if that NPC is on-screen.	
        return bankers[0].isOnScreen(); // Return whether or not the banker is on	
                                    // the screen. If it is, we are in the			
                                    // bank; if not, then we are not in the		
                                    // bank.
    }//isInBank

    private boolean cut() {
        if (isCutting()) {
            final long timeout = System.currentTimeMillis() + General.random(60000, 90000); 
            // Let's define a timeout for the loop below. If we don't have a
            // timeout, it is possible that the script will enter an infinite
            // loop, and will therefore be stuck. We set the timeout for the
            // current time plus somewhere between 60 and 90 seconds. We use
            // randomness to avoid seeming bot-like.
            while (isCutting() && System.currentTimeMillis() < timeout) {
                sleep(100, 150); // We will loop while we are cutting, and while the current time			
                // is before the timeout. Make sure to have a sleep to prevent a	CPU overload.				
                // TODO: We could also implement some anti-ban features here if we	
                // want.			
                // Now, let's check if the willow tree is still at the location	
                // of the last clicked location. To do this, we will define a	
                // global variable - last_tree_tile - of type RSTile. The	
                // variable will be defined below, when we find and click a	
                // tree.			
                // Make sure to null check the variable, since it can be null.
                // If it is null and we try to perform actions upon it, a Null	
                // Pointer Exception will be thrown, crashing the script.	
                if (this.last_tree_tile != null) {
                    // The variable is not null. We can use it now.			
                    if (!Objects.isAt(this.last_tree_tile, "Willow")) {
                        // The willow tree is gone. It has either been chopped		
                        // down, or turned into an ent. Let's break out of this		
                        // loop.	
                        break;
                    }
                }
        }

    }//cut


    // Let's go find a tree to chop.
    final RSObject[] trees = Objects.findNearest(50, "Willow");
    // Search for the willow within 50	tiles
    if (trees.length < 1)
        return false; // No trees have been found. We can't do anything.
    if (!trees[0].isOnScreen()) {
        // The nearest tree is not on the screen. Let's walk to it.	
        if (!Walking.walkPath(Walking.generateStraightPath(trees[0]))) 
            // We could not walk to the tree. Let's exit so we don't try	
            // clicking a tree which isn't on screen.
            return false;

    if (!Timing.waitCondition(new Condition() { 
        // We will now use the Timing API to wait until the tree is on	
        // the screen (we are probably walking to the tree right now).	
        @Override
        public boolean active() {
            General.sleep(100); // Sleep to reduce CPU usage.
            return trees[0].isOnScreen();
        }
        }, General.random(8000, 9300)))
        // A tree could not be found before the timeout of 8-9.3	
        // seconds. Let's exit the method and return false. we don't		
        // want to end up trying to click a tree which isn't on the
        // screen.		
        return false;
    }

    // Okay, now we are sure trees[0] is on-screen. Let's click it. We may	
    // be still moving at this moment, so let's use DynamicClicking.		
    // DynamicClicking should be used when your character is moving, or the	
    // target is moving, and you need to click the target.	
    if (!DynamicClicking.clickRSObject(trees[0], "Chop down")) 
        // We could not click the tree. Let's exit the method since we		
        // failed.
        return false;
    // We clicked the tree. Let's first wait to stop chopping for 1-1.2	
    // seconds just in case we moved on to this tree while still performing		
    // the chopping animation.	
    Timing.waitCondition(new Condition() {
        @Override
        public boolean active() {
            return !isCutting();
        }
    }, General.random(1000, 1200));
    // We don't need to if check it since the result doesn't matter.		
    if (Timing.waitCondition(new Condition() {
        // Now let's wait until we are cutting.			
        @Override
        public boolean active() {
            return isCutting();
        }
        }, General.random(8000, 9000))) {
            // We are cutting! Now let's record the tree's tile and return true.			
            this.last_tree_tile = trees[0].getPosition().clone(); 
            // getPosition() can never be null, so we don't need to null check		
            // it.		
            return true;
        }
        // We failed to cut a tree. Return false.	
        return false;
    }//cut

    private boolean isCutting() {
        return Player.getAnimation() > 0; // If the animation ID is greater than			
        // 0, then we are animating. Let's		
        // assume that if were are							
        // animating, that the animation is		
        // the chopping one.	
    }

    private boolean walkToBank() {
        if (!WebWalking.walkToBank()) { // We failed to walk to the bank. Let's return false.	
            return false;
        }
            // Walking succeeded, but we may still be moving, and not our destination	
            // yet. Let's wait until we are in the bank.	
        return Timing.waitCondition(new Condition() { // If we reach the bank before the timeout, this method will return		
                                                        // true. Otherwise, it will return false.			
            @Override public boolean active() {
            General.sleep(200, 300); // Reduces CPU usage.		
                return isInBank();
            }
        }, General.random(8000, 9000));
    }

    private boolean walkToTrees() {
        final RSObject[] willows = Objects.findNearest(50, "Willow");
        if (willows.length < 1) // No willows could be found. We cannot do anything. Let's exit this				
            return false;
        // Let's walk to the closest willow tree now.	
        if (!WebWalking.walkTo(willows[0]))
        // We failed to walk to the bank. Let's return false.	
            return false;
        // Walking failed, but we may still be moving, and not our destination	
        // yet. Let's wait until we are at the trees.		
        return Timing.waitCondition(new Condition() { // If we reach the trees before the timeout, this method will return	
                                                      // true. Otherwise, it will return false.	
        @Override public boolean active() {
        General.sleep(200, 300); // Reduces CPU usage.	
            return isAtTrees();
        }
        }, General.random(8000, 9000));
    }

    private boolean bank() {

        if (!Banking.isBankScreenOpen()) { // The bank screen is not open. Let's open it.		
            if (!Banking.openBank())
            // Could not open the bank. Let's return false.		
                return false;
                // Since openBank() will wait for the bank screen to be open before	
                // returning true, it is safe to assume that the bank screen is now		
                // open.		
        }
        // Now let's deposit the logs. We will check if an axe is in our	
        // inventory. If there is one, we will bank by using our inventory	
        // items. Otherwise, we will just click the deposit all button.	
        // Let's define our axe names now. It is better to define variables like	
        // this globally, but we only use the axe names within this method, so I	
        // don't feel the need to.		
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
            // If the length of the returned value if greater than one, that		
            // means we have an axe in our inventory.	
            if (Banking.depositAllExcept(axe_names) < 1)
                // We failed to deposit our items. Let's exit and return false.		
                return false;
        } 
        else {
            if (Banking.depositAll() < 1)
            // We failed to click the deposit all button. Let's exit and return false.	
            return false;
        }
       // Okay, our items should get deposited. Let's wait and make sure they
       // get deposited.		
       return Timing.waitCondition(new Condition() { // Since we can only enter the bank method if our inventory is full,	
                                                     // let's wait until our inventory is not full. If it is not full	
                                                     // before the timeout, return true. Otherwise, return false.		
            @Override
            public boolean active() {
            return !Inventory.isFull();
            }
        }, General.random(3000, 4000));
    }//bank

    @Override
    public void run() {
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
            // Time to check what to do. If the inventory is full, we should	
            // bank the items. Otherwise, we should walk back to the trees.		
            if (Inventory.isFull())      
                bank();
            else
                walkToTrees();
            } 
            else { // We are neither in the bank, nor at the willows		
                // Time to check what to do. If the inventory is full, we will	
                // walk to the bank. Otherwise, we will walk to the willows.	
            if (Inventory.isFull())
                walkToBank();		
            else
                walkToTrees();
            }
        }//while 
    }//run
}//TutorialScript