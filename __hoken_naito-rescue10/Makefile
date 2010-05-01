
JAVAC?=javac
JAVACOPT := -J-Dfile.encoding=UTF8
JAR?=jar

SOURCEDIR := src
DESTDIR := classes

CLASSFILE := $(shell find $(DESTDIR) -name "*.class")
SOURCES := $(shell find $(SOURCEDIR) -name "*.java")

PACKAGE_TOP := naito_rescue
JARFILENAME := $(PACKAGE_TOP).jar

CLASSPATH := $(shell find ./jars -name "*.jar" | xargs | sed -e "s/ /:/g")

all:
	echo "__hoken make."
	$(JAVAC) $(JAVACOPT) -d $(DESTDIR) -classpath $(CLASSPATH) $(SOURCES)
	$(JAR) cvf $(JARFILENAME) -C $(DESTDIR) .

.PHONY: clean 

clean:
	rm $(CLASSFILE)
	rm $(JARFILENAME)
