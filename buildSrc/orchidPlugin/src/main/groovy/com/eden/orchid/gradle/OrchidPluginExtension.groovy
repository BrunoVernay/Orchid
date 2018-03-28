package com.eden.orchid.gradle

class OrchidPluginExtension {
    boolean noJavadoc
    String srcDir
    String destDir
    String runTask
    String version
    String theme
    String baseUrl
    String environment
    List<String> args = new ArrayList<String>()
}
