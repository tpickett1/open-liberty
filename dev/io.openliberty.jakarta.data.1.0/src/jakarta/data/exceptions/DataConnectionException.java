/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package jakarta.data.exceptions;

/**
 * copied from Jakarta Data git repository
 */
public class DataConnectionException extends DataException {
    private static final long serialVersionUID = 4736774083679114892L;

    public DataConnectionException(String message) {
        super(message);
    }

    public DataConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataConnectionException(Throwable cause) {
        super(cause);
    }
}
