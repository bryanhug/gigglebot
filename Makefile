.PHONY: test run clean loader

loader:
	java -jar TriBot_Loader.jar &

bot:
	javac -cp TRiBot.jar -d ~/Library/Application\ Support/tribot/bin gigglebot/scripts/WoodCutting.java
