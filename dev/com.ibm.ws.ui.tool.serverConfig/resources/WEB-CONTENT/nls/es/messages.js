/*******************************************************************************
 * Copyright (c) 2016, 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

var editorMessages = {
"VALUE": "Valor",
"SELECT": "Seleccionar",
"ADD": "Añadir",
"OK": "Aceptar",
"CANCEL": "Cancelar",
"ADD_CHILD": "Añadir hijo",
"REMOVE": "Eliminar",
"TEST" : "Probar",
"TEST_CONNECTION": "Conexión de prueba",
"DESCRIPTION": "Descripción",
"PREVIOUS": "Anterior",
"NEXT": "Siguiente",
"REMOVE_ELEMENT_CONFIRMATION": "¿Está seguro de que desea eliminar el elemento?",
"YES": "Sí",
"NO": "No",
"LOADING": "Cargando...",
"SERVER_CONFIGURATION_EDITOR": "Herramienta de configuración de servidor",
"CONFIGURATION_FILES": "Archivos de configuración",
"TOGGLE_NAVIGATION": "Conmutar navegación",
"CLOSE": "Cerrar",
"ERROR_ACCESSING_SERVER_CONFIGURATION_FILE": "No se ha podido acceder al archivo de configuración de servidor {0}.",
"ERROR_ACCESSING_SERVER_SCHEMA_FILE": "No se ha podido acceder al archivo de esquema {0}.",
"NO_MATCHES_FOUND": "No se han encontrado coincidencias.",
"DEFAULT_VALUE_PLACEHOLDER": "{0} (valor predeterminado)",
"DEFAULT_VALUE_PLACEHOLDER_WITH_VARIABLE": "{0} (valor predeterminado) o ${{1}} (si se ha definido)",
"VARIABLE_VALUE_PLACEHOLDER": "{0} (si se ha definido)",
"DEFAULT_SUFFIX": "(valor predeterminado)",
"SAVE": "Guardar",
"DISCARD": "Descartar",
"ENHANCED_LABELS": "Etiquetas ampliadas",
"FIELD_DESCRIPTIONS": "Descripciones de campo",
"MALFORMED_XML": "Parece que el contenido XML está mal formado, conmute a {0} para editarlo.",
"DESIGN": "Diseñar",
"SOURCE": "Origen",
"SOURCE_PANE": "panel de origen",
"LINE_NUMBERS": "Números de línea",
"DOCUMENTATION_DEFAULT": "Valor predeterminado: {0}",
"REQUIRED_SUFFIX": "(necesario)",
"USER_NAME": "Nombre de usuario",
"USER_NAME_OPTIONAL": "Nombre de usuario (opcional)",
"PASSWORD": "Contraseña",
"PASSWORD_OPTIONAL": "Contraseña (opcional)",
"SIGN_IN": "Inicio de sesión",
"ONE_MOMENT_PLEASE": "Un momento, por favor...",
"LOGIN_FAIL": "El inicio de sesión ha fallado, inténtelo de nuevo",
"FAILED" : "Ha fallado.",
"FAILED_HTTP_CODE" : "La solicitud ha fallado con el código de estado HTTP {0}.",
"SUCCESS" : "Satisfactorio.",
"RESPONSE": "Respuesta",
"PARAMETERS": "Parámetros",
"MISSING_USER_NAME": "Especifique su nombre de usuario", 
"SIGN_OUT": "Salir de sesión",
"SIGN_OUT_ERROR": "Se ha producido un error al intentar salir de la sesión",
"SIGN_OUT_PROMPT": "¿Salir de la sesión?",
"CHANGES_SAVED": "Cambios guardados",
"FILE_ACCESS_ERROR_MESSAGE": "Se ha producido un error al intentar acceder a los archivos de configuración de servidor",
"ERROR_SAVING_FILE_MESSAGE": "Los cambios no se han guardado. Es posible que el servidor esté inactivo, puede que el archivo sea de sólo lectura o puede que esté en un rol que no tiene permiso para realizar cambios de configuración.",
"ERROR": "Error",
"UNSAVED_CHANGES_MESSAGE": "Hay cambios no guardados.",
"READ_ONLY": "Sólo lectura",
"RESTRICTED_OR_UNAVAILABLE": "Restringido o no disponible",
"EXPLORE_INCLUDES": "Explorar inclusiones",
"OPEN": "Abrir",
"READ_ONLY_WARNING_MESSAGE": "No se ha configurado el acceso a archivos remotos en este servidor. Los archivos están disponibles en modalidad de sólo lectura. Para habilitar el acceso de escritura, añada el siguiente elemento al archivo server.xml:",
"SAVE_BEFORE_CLOSING_DIALOG_TITLE": "Cerrar",
"SAVE_BEFORE_CLOSING_DIALOG_MESSAGE": "¿Desea guardar los cambios en {0} antes de cerrar?",
"DONT_SAVE": "No guardar",
"RETURN_TO_EDITOR": "Volver al editor",
"THE_VALUE_SHOULD_BE_A_BOOLEAN": "EL valor debe ser verdadero o falso.",
"THE_VALUE_SHOULD_BE_A_NUMBER": "El valor debe ser un número.",
"THE_VALUE_SHOULD_BE_AMONG_THE_POSSIBLE_OPTIONS": "El valor debe estar entre las opciones posibles: {0}.",
"UNRECOGNIZED_ELEMENT": "El servidor no reconoce el elemento \"{0}\". Conmute a {1} para poder editar el contenido.",
"SERVER_NOT_FOUND": "No se ha encontrado el servidor \"{0}\" con el host \"{1}\" y el directorio de usuario \"{2}\".",
"FILE_NOT_FOUND_REPLACE": "No se ha encontrado el archivo \"{0}\".",
"ERROR_ACCESSING_SERVER_LIST": "No se ha podido acceder a la lista de servidores del colectivo.",
"ERROR_NOT_IN_COLLECTIVE_ENVIRONMENT": "La referencia de servidor remoto del URL no es válida fuera de un entorno colectivo. Pulse {0} para acceder a los archivos de configuración en el servidor local.",
"ERROR_ACCESSING_INCLUDE_FILES": "No se ha podido acceder a los archivos de inclusión.",
"PATH_NOT_AVAILABLE": "Vía de acceso no disponible",
"HERE": "aquí",
"CHANGE_SERVER": "Cambiar servidor",
"ERROR_RETRIEVING_SERVER_INFORMATION": "No se ha podido recuperar la información de servidor.",
"SELECT_SERVER": "Seleccionar servidor",
"SERVER_DESCRIPTION": "Seleccione un elemento en el árbol del lado izquierdo para visualizar la configuración.",
"SELECT_ELEMENT_TO_VIEW_DESCRIPTION": "Seleccione un elemento para ver la descripción.",
"SAVING": "Guardando...",
"SERVER_NAME": "Nombre de servidor",
"CLUSTER": "Clúster",
"HOST": "Host",
"USER_DIRECTORY_PATH": "Vía de acceso de directorio de usuario",
"SERVERS": "{0} servidores",
"ONE_SERVER": "1 servidor",
"SHOWING_FIRST_N_SERVERS": "(Mostrando los 500 primeros servidores)",
"COULD_NOT_RETRIEVE_SERVER_IDENTIFICATION": "No se ha podido recuperar la información de identificación de servidor.",
"CONTENT_ASSIST_AVAILABLE": "Pulse Ctrl+espacio para obtener asistencia de contenido.",
"OPEN_FILE": "Abrir archivo",
"CREATE_FILE": "Crear archivo",
"FILE_NOT_FOUND": "No se ha encontrado el archivo",
"CANNOT_ACCESS_FILE": "No se puede acceder al archivo",
"CREATING_FILE": "Creando archivo...",
"SUCCESSFULLY_CREATED_FILE": "Se ha creado correctamente el archivo",
"COULD_NOT_CREATE_FILE": "No se ha podido crear el archivo",
"FILE_CHANGED_DURING_EDITING_DIALOG_TITLE": "Conflicto al guardar",
"OVERWRITE": "Sobrescribir",
"FILE_CHANGED_DURING_EDITING_DIALOG_MESSAGE": "Un usuario o proceso diferente ha cambiado el archivo {0} después de abrirlo.",
"OVERWRITING": "Sobrescribiendo...",
"SEARCH": "Buscar",
"SETTINGS": "Valores",
"LOCATION": "Ubicación",
"EXPAND": "Expandir",
"COLLAPSE": "Contraer",
"CLEAR": "Borrar",
"EXPAND_COLLAPSE": "Expandir/contraer",
"ELEMENT_INFORMATION_FORM": "Formulario de información de elemento",
"SOURCE_EDITOR": "Editor de origen",
"SOURCE_EDITOR_CONTENT": "Contenido de editor de origen",
"SOURCE_EDITOR_MENU" : "Contenido de menú",
"ELEMENT_DESCRIPTION": "Descripción de elemento",
"ELEMENT_SEARCH": "Búsqueda de elemento",
"ADD_CHILD_ELEMENT_DIALOG": "Diálogo Añadir elemento hijo",
"REMOVE_ELEMENT_DIALOG": "Diálogo Eliminar elemento",
"VALIDATE_DATASOURCE_DIALOG": "Diálogo Validar conexión de origen de datos",
"VALIDATE_DATASOURCE": "Probando conexión de origen de datos",
"ENUMERATION_SELECTION_DIALOG": "Diálogo Selección de enumeración",
"SAVE_BEFORE_CLOSING_DIALOG": "Diálogo Guardar antes de cerrar",
"ERROR_SAVING_FILE_DIALOG": "Diálogo Error al guardar archivo",
"FILE_CHANGED_DURING_EDITING_DIALOG": "Diálogo Archivo cambiado durante edición",
"SERVER_TABLE_CELL_FOR_SCREEN_READER": "Clúster {0}, Host {1}, vía de acceso de directorio de usuario {2}",
"WARNING": "Aviso",
"EMPTY_STRING_ATTRIBUTE_VALUE": "(serie vacía) pulse el botón borrar para eliminar el atributo",
"EMPTY_STRING_ELEMENT_VALUE": "(serie vacía)",
"NO_VALUE": "(sin valor)",
"DEFAULTS": "Valores por omisión",
"PRIMARY": "Primario",
"AUTH_ALIAS_OPTIONAL": "Alias de autenticación (opcional)",
"AUTH_ALIAS": "Alias de autenticación",
"CONTAINER_AUTHENTICATION": "Autenticación de contenedor",
"NO_RESOURCE_REFERENCE": "Ninguna referencia de recurso",
"APPLICATION_AUTHENTICATION": "Autenticación de aplicación",
"CUSTOM_LOGIN_MODULE": "Módulo de inicio de sesión personalizado (opcional)",
"LOGIN_PROPERTIES": "Propiedades de inicio de sesión (opcional)",
"TEST_RESULTS": "Resultados de prueba de origen de datos",
"OVERRIDES": "Alteraciones temporales",
"SELECT_FEATURE_DIALOG": "Diálogo Seleccionar característica",
"SELECT_FEATURE": "Seleccionar característica",
"FEATURE_DESCRIPTION": "Descripción de característica",
"SELECT_FEATURE_TO_VIEW_DESCRIPTION": "Seleccione una característica para visualizar la descripción.",
"SUPPORT_MESSAGE" : "La configuración de servidor no está disponible para los servidores Node.js ni los servidores en un contenedor Docker al que se accede a través del controlador colectivo.",
"NO_ROLE_MESSAGE": "El usuario no está en un rol que tenga permiso para realizar cambios de configuración. Los archivos están disponibles en modalidad de sólo lectura.",
	
// Messages for Collective Debugging
"REQUIRED_ACTIONS" : "ACCIONES NECESARIAS PARA MIEMBROS DE COLECTIVO:",
"RUN_UPDATE_HOST" : "Ejecute 'collective updateHost' o 'collective registerHost' con '--hostWritePath' especificando una vía de acceso a la que rpcUser tenga permisos de escritura.  También se deben proporcionar credenciales de host válidas.",
"REGISTERING_HOST_LINK" : "Registro de sistemas principales con un colectivo de Liberty",
"CONIFGURED_SSH_RXA" : "El servidor miembro debe tener SSH o hostAuthInfo configurados apropiadamente en la configuración.",
"CONFIGURING_COLLECTIVE_LINK" : "Configurando colectivo de Liberty",
"CONFIGURING_RXA_LINK" : "Configurando RXA",
"TWO_LINKS" : "{0} y {1}", //Hyperlink 1 (CONFIGURING_COLLECTIVE_LINK) and hyperlink 2 (CONFIGURING_RXA_LINK)
"CONFIGRUED_READ_DIR" : "Asegúrese de que la configuración del miembro especifica acceso remoteFileAccess readDir a los archivos de configuración que deben ser accesibles mediante la configuración de servidor.",
"DEFAULT_READ_DIR" : "Nota: De forma predeterminada (y en ausencia de remoteFileAccess especificado en la configuración), están disponibles los readDirs siguientes: ${wlp.install.dir}, ${wlp.user.dir} y ${server.output.dir}.  De forma predeterminada no hay disponibles ningún writeDir.",
"PUBLISHED_READ_DIR" : "El servidor miembro debe estar en ejecución o debe haberse iniciado anteriormente y haber publicado correctamente los readDirs remoteFileAccess.",
"JAVA_AVAILABLE" : "Asegúrese de que JAVA esté establecido en la vía de acceso. Hay 3 maneras de establecer JAVA:",
"HOST_JAVA_HOME" : "Especificando el parámetro --hostJAVAHome en el mandato collective updateHost. Por ejemplo ./collective updateHost --host=[nombreHost] --user=[usuario] --password=[contraseña] --port=[puertoHttps] --hostJAVAHome=[víaAccesoAJAVA]",
"LINK_JAVA" : "Cree un enlace a JAVA en /usr/bin. Por ejemplo /usr/bin/java -> [víaAccesoAJAVA]",
"JAVA_ON_PATH" : "Añada JAVA a la variable de entorno PATH.  La exportación de PATH se debe realizar en .bashrc en Linux.", 
"MORE_INFORMATION" : "Para más información, consulte: "

};