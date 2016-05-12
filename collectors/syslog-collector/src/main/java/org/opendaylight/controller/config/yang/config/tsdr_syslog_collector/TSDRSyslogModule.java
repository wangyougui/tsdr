/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.config.yang.config.tsdr_syslog_collector;

import org.opendaylight.tsdr.syslogs.TSDRSyslogCollectorImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.tsdr.collector.spi.rev150915.TsdrCollectorSpiService;

public class TSDRSyslogModule extends org.opendaylight.controller.config.yang.config.tsdr_syslog_collector.AbstractTSDRSyslogModule {
    public TSDRSyslogModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public TSDRSyslogModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.controller.config.yang.config.tsdr_syslog_collector.TSDRSyslogModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        super.customValidation();
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        getDataBrokerDependency();
        final TSDRSyslogCollectorImpl impl = new TSDRSyslogCollectorImpl(getRpcRegistryDependency().getRpcService(TsdrCollectorSpiService.class));
        return new AutoCloseable() {
            @Override
            public void close() throws Exception {
                impl.close();
            }
        };
    }

}