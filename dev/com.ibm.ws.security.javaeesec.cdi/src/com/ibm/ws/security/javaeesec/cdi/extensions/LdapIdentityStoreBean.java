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
package com.ibm.ws.security.javaeesec.cdi.extensions;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.TypeLiteral;
import javax.security.enterprise.identitystore.IdentityStore;
import javax.security.enterprise.identitystore.LdapIdentityStoreDefinition;

import com.ibm.websphere.ras.Tr;
import com.ibm.websphere.ras.TraceComponent;
import com.ibm.ws.security.javaeesec.identitystore.LdapIdentityStore;

/**
 * TODO: Determine if this bean can be PassivationCapable.
 */
public class LdapIdentityStoreBean implements Bean<IdentityStore>, PassivationCapable {

    private static final TraceComponent tc = Tr.register(LdapIdentityStoreBean.class);
    private final Set<Annotation> qualifiers;
    private final Type type;
    private final Set<Type> types;
    private final String name;
    private final String id;
    private final LdapIdentityStoreDefinition ldapIdentityStoreDefinition;

    public LdapIdentityStoreBean(BeanManager beanManager, LdapIdentityStoreDefinition ldapIdentityStoreDefinition) {
        this.ldapIdentityStoreDefinition = ldapIdentityStoreDefinition;
        qualifiers = new HashSet<Annotation>();
        qualifiers.add(new AnnotationLiteral<Default>() {
        });
        qualifiers.add(new AnnotationLiteral<Any>() {
        });

        type = new TypeLiteral<IdentityStore>() {
        }.getType();
        types = Collections.singleton(type);
        name = this.getClass().getName() + "@" + this.hashCode() + "[" + type + "]";
        id = beanManager.hashCode() + "#" + this.name;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.enterprise.context.spi.Contextual#create(javax.enterprise.context.spi.CreationalContext)
     */
    @Override
    public IdentityStore create(CreationalContext<IdentityStore> arg0) {
        return new LdapIdentityStore(ldapIdentityStoreDefinition);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.enterprise.context.spi.Contextual#destroy(java.lang.Object, javax.enterprise.context.spi.CreationalContext)
     */
    @Override
    public void destroy(IdentityStore arg0, CreationalContext<IdentityStore> arg1) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see javax.enterprise.inject.spi.BeanAttributes#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.enterprise.inject.spi.BeanAttributes#getQualifiers()
     */
    @Override
    public Set<Annotation> getQualifiers() {
        // TODO Auto-generated method stub
        return qualifiers;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.enterprise.inject.spi.BeanAttributes#getScope()
     */
    @Override
    public Class<? extends Annotation> getScope() {
        return ApplicationScoped.class;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.enterprise.inject.spi.BeanAttributes#getStereotypes()
     */
    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.enterprise.inject.spi.BeanAttributes#getTypes()
     */
    @Override
    public Set<Type> getTypes() {
        return types;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.enterprise.inject.spi.BeanAttributes#isAlternative()
     */
    @Override
    public boolean isAlternative() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.enterprise.inject.spi.Bean#getBeanClass()
     */
    @Override
    public Class<?> getBeanClass() {
        return IdentityStore.class;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.enterprise.inject.spi.Bean#getInjectionPoints()
     */
    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        // TODO Auto-generated method stub
        return Collections.emptySet();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.enterprise.inject.spi.Bean#isNullable()
     */
    @Override
    public boolean isNullable() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.enterprise.inject.spi.PassivationCapable#getId()
     */
    @Override
    public String getId() {
        return id;
    }
}
