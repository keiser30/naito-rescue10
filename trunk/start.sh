# $1 Server IP
# $2 Server port
# $3 FB
# $4 AT
# $5 PF
# $6 FS
# $7 AC
# $8 PO
# $9 Client number

CLASSPATH=`find ./jars -name "*.jar" | xargs | sed -e "s/ /:/g"`
#CLASSPATH=$CLASSPATH:./naito_rescue.jar

echo $CLASSPATH

java -Xmx2048m -cp .:$CLASSPATH rescuecore2.LaunchComponents naito_rescue.agent.NAITOFireBrigade*$3 naito_rescue.agent.NAITOPoliceForce*$5  naito_rescue.agent.NAITOAmbulanceTeam*$4 sample.SampleCentre*n -h $1 --loadabletypes.inspect.dir=./jars --random.seed=1
