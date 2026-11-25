SISTEMA DE PEDIDOS PARA UN LOCAL CON PRODUCTOS PERSONALIZABLES.

Proyecto Java (Apache Ant, abrible en NetBeans) para el parcial de Patrones de Diseño (Ingeniería de Software II, Universidad Libre).

Un único núcleo de dominio sirve a dos interfaces: consola y GUI Swing.





Patrones: Decorator · Builder · Factory Method · Observer · Strategy 

Arquitectura: Monolito por capas: Presentación → Lógica de negocio → Acceso a datos → Persistencia (SQLite)

Lenguaje: Java 11+ (probado con JDK 21), sin frameworks. Solo Swing y JDBC (`java.sql`)

Pruebas: 82 pruebas JUnit 4 (una clase por patrón + flujo completo + variantes en vivo + SQLite



INSTRUCCIONES PARA COMPILAR Y EJECUTAR

1\. Requisitos previos



Para compilar y ejecutar el proyecto se necesita:



JDK 11 o superior (se recomienda JDK 17 o 21).



Windows 10 o superior.



CMD o PowerShell.



No es necesario instalar Apache Ant ni utilizar un entorno de desarrollo para ejecutar la aplicación.



Para verificar que Java está correctamente instalado, abre CMD y ejecuta:



**java -version**

**javac -version**



Ambos comandos deben mostrar la versión instalada de Java.



2\. Descargar y preparar el proyecto



Descarga el archivo ZIP del proyecto.



Descomprime el archivo en tu equipo.



Abre la carpeta principal cafeteria-rosetta.



Verifica que se encuentren las carpetas src, lib, docs y los archivos del proyecto.



3\. Compilación del proyecto



La compilación se realiza directamente desde CMD utilizando el compilador javac incluido en el JDK.



Paso 1. Abrir CMD en la carpeta del proyecto



Ubícate en la carpeta principal cafeteria-rosetta.



Paso 2. Compilar todos los archivos Java



Copia y ejecuta el siguiente comando:



**powershell -NoProfile -Command "$files=Get-ChildItem -Path src -Filter \*.java -Recurse | ForEach-Object {$\_.FullName}; New-Item -ItemType Directory -Force build\\classes | Out-Null; javac -encoding UTF-8 -d build\\classes $files"**



Este comando realiza las siguientes operaciones:



Busca todos los archivos .java dentro de src y sus subcarpetas.



Crea automáticamente la carpeta build\\classes.



Compila todas las clases del proyecto.



Organiza los archivos compilados respetando la estructura de paquetes.



Paso 3. Copiar los recursos del proyecto



Ejecuta el siguiente comando para copiar imágenes y demás recursos necesarios para la aplicación:



**powershell -NoProfile -Command "$base=(Resolve-Path 'src').Path; Get-ChildItem 'src' -Recurse -File | Where-Object {$\_.Extension -ne '.java'} | ForEach-Object {$rel=$\_.FullName.Substring($base.Length).TrimStart('\\'); $dest=Join-Path 'build\\classes' $rel; New-Item -ItemType Directory -Force (Split-Path $dest) | Out-Null; Copy-Item $\_.FullName $dest -Force}"**



Una vez finalizado este proceso, el proyecto estará compilado y listo para ejecutarse.



4\. Ejecutar la interfaz gráfica (GUI)



Para iniciar la aplicación con su interfaz gráfica, ejecuta desde CMD:



**java -cp build\\classes cafeteria.Main gui**



Se abrirá la ventana principal de Cafetería Rosetta, desde donde podrás acceder a las funcionalidades del sistema.



5\. Ejecutar la interfaz por consola



Para iniciar la versión de consola, ejecuta:



**java -cp build\\classes cafeteria.Main consola**



Se mostrará el menú principal de Cafetería Rosetta directamente en CMD.



El usuario podrá ingresar las opciones correspondientes y navegar por las funcionalidades del sistema utilizando el teclado.



6\. Ejecutar el proyecto en modo memoria



El sistema también permite ejecutarse sin persistencia de datos, utilizando únicamente almacenamiento en memoria.



Interfaz gráfica:



**java -cp build\\classes cafeteria.Main gui --memoria**



Interfaz de consola:



**java -cp build\\classes cafeteria.Main consola --memoria**



En este modo, los datos se mantienen durante la ejecución de la aplicación, pero no se conservan al cerrarla.



7\. Persistencia de datos con SQLite



El proyecto permite utilizar SQLite para almacenar información de manera persistente.



Para habilitar esta funcionalidad:



Descarga el controlador JDBC de SQLite (sqlite-jdbc) desde Maven Central.



Coloca el archivo .jar del controlador dentro de la carpeta lib.



Ejecuta nuevamente los pasos de compilación.



Inicia la aplicación sin utilizar el argumento --memoria.



Si el controlador está disponible, el sistema utilizará SQLite para guardar los productos, pedidos, ventas y cierres de caja.



Si no se encuentra el controlador, el sistema mostrará una advertencia y funcionará automáticamente en modo memoria.



8\. Estructura de compilación



Después de compilar el proyecto, se generará la siguiente estructura:



cafeteria-rosetta/

│

├── src/

│   └── cafeteria/

│       ├── Main.java

│       ├── app/

│       ├── dominio/

│       ├── service/

│       ├── repository/

│       └── ui/

│           ├── consola/

│           └── gui/

│

├── lib/

│   ├── junit-4.13.2.jar

│   └── hamcrest-2.2.jar

│

├── build/

│   └── classes/

│       └── cafeteria/

│           └── ...

│

├── docs/

├── README.md

└── ejecutar-consola.bat



9\. Consideraciones finales



Ambas interfaces utilizan las mismas clases y servicios de negocio.



No es necesario compilar dos aplicaciones independientes.



La compilación se realiza directamente con el JDK.



No se requiere instalar Apache Ant.



Para ejecutar nuevamente el proyecto después de modificar el código fuente, es necesario repetir los pasos de compilación.



El modo memoria permite probar todas las funcionalidades sin conservar información entre ejecuciones.

