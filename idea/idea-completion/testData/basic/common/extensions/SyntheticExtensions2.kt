fun Thread.foo(urlConnection: java.net.URLConnection) {
    with (urlConnection) {
        <caret>
    }
}

// EXIST_JAVA_ONLY: { lookupString: "priority", itemText: "priority", tailText: " (from getPriority()/setPriority())", typeText: "Int" }
// EXIST_JAVA_ONLY: { lookupString: "daemon", itemText: "daemon", tailText: " (from isDaemon()/setDaemon())", typeText: "Boolean" }
// EXIST_JAVA_ONLY: { lookupString: "URL", itemText: "URL", tailText: " (from getURL())", typeText: "URL!" }
// ABSENT: getPriority
// ABSENT: setPriority
// ABSENT: isDaemon
// ABSENT: setDaemon
// ABSENT: getURL
