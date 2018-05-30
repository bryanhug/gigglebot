.PHONY: test run clean loader

SRC_DIR=gigglebot/src

loader:
	java -jar TriBot_Loader.jar 

test:
	javac -cp TRiBot.jar -d gigglebot/classes gigglebot/src/intro/tutorial/TutorialScript.java

run:
	java -classpath gigglebot/classes/intro/tutorial TutorialScript

clean:
	rm -rf gigglebot/classes/intro/