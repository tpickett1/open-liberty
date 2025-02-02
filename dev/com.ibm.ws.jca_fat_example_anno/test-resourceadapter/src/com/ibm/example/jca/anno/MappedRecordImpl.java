/*******************************************************************************
 * Copyright (c) 2017, 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.example.jca.anno;

import java.util.TreeMap;

import jakarta.resource.cci.MappedRecord;

/**
 * Example MappedRecord where entries are kept in a TreeMap.
 */
public class MappedRecordImpl<K, V> extends TreeMap<K, V> implements MappedRecord<K, V> {
    private static final long serialVersionUID = 5653529590057147554L;

    private String recordName;
    private String recordShortDescription;

    @Override
    public String getRecordName() {
        return recordName;
    }

    @Override
    public String getRecordShortDescription() {
        return recordShortDescription;
    }

    @Override
    public void setRecordName(String recordName) {
        this.recordName = recordName;
    }

    @Override
    public void setRecordShortDescription(String recordShortDescription) {
        this.recordShortDescription = recordShortDescription;
    }

}
