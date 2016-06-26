classpath="./conf"
for file in lib/*.jar
do
  if [[ ! -f "$file" ]]
  then
      continue
  fi
  classpath="$classpath:$file"
done
echo $classpath
JAVA_OPTS="-Duser.timezone=GMT+08 -Xmx64m -XX:MaxDirectMemorySize=32m"
nohup java $JAVA_OPTS -cp $classpath org.tiger.ant.FileServer 27110 >> out.log &
pid=$!
echo $pid>ant.pid