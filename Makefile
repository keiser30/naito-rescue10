
JAVAC?=javac
JAVACOPT := -J-Dfile.encoding=UTF8
JAR?=jar

SOURCEDIR := src
DESTDIR := classes

CLASSFILE := $(shell find $(DESTDIR) -name "*.class")
SOURCES := $(shell find $(SOURCEDIR) -name "*.java")

PACKAGE_TOP := naito_rescue
JARFILENAME := $(PACKAGE_TOP).jar

KERNEL_BASE := /home/robocup/rescue/rescue-nightly/0407
# KERNEL_BASE := /Users/robocup/rescue/rescue-nightly/0403
CLASSPATH := $(shell find $(KERNEL_BASE)/jars -name "*.jar" | xargs | sed -e "s/ /:/g")

all:
	echo $(SOURCES)
	echo $(CLASSPATH)
	$(JAVAC) $(JAVACOPT) -d $(DESTDIR) -classpath $(CLASSPATH) $(SOURCES)
	$(JAR) cvf $(JARFILENAME) -C $(DESTDIR) .

.PHONY: clean 

clean:
	rm $(CLASSFILE)
	rm $(JARFILENAME)
