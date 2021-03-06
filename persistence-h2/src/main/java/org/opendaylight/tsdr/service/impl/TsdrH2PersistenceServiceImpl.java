/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.tsdr.service.impl;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.tsdr.entity.Metric;
import org.opendaylight.tsdr.spi.persistence.TsdrPersistenceService;
import org.opendaylight.tsdr.spi.util.FormatUtil;
import org.opendaylight.tsdr.spi.util.TsdrPersistenceServiceUtil;
import org.opendaylight.yang.gen.v1.opendaylight.tsdr.rev150219.DataCategory;
import org.opendaylight.yang.gen.v1.opendaylight.tsdr.rev150219.TSDRRecord;
import org.opendaylight.yang.gen.v1.opendaylight.tsdr.rev150219.storetsdrlogrecord.input.TSDRLogRecord;
import org.opendaylight.yang.gen.v1.opendaylight.tsdr.rev150219.storetsdrmetricrecord.input.TSDRMetricRecord;
import org.opendaylight.yang.gen.v1.opendaylight.tsdr.rev150219.tsdrlog.RecordAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the TSDR Persistence SPI utilizing JPA based store
 *
 * Note; The JPA service is injected via blueprint framework
 *
 *
 * @author <a href="mailto:syedbahm@cisco.com">Basheeruddin Ahmed</a>
 *
 */

public class TsdrH2PersistenceServiceImpl implements
    TsdrPersistenceService {

    private final static Logger log = LoggerFactory.getLogger(TsdrH2PersistenceServiceImpl.class);
    private static final String SEPARATOR = "_";

    private  TsdrJpaServiceImpl jpaService;

    public TsdrH2PersistenceServiceImpl(){
        TsdrPersistenceServiceUtil.setTsdrPersistenceService(this);
        log.info("TSDR H2 Data Store initialized.");
        System.out.println("TSDR H2 Data Store initialized."
            + " Please do not install another TSDR Data Store without uninstalling H2 data store.");
    }

    /**
     * Store TSDRMetrics.
     */
    @Override
    public void store(TSDRMetricRecord metrics){
        Preconditions.checkArgument(metrics != null);
        DataCategory dc =metrics.getTSDRDataCategory();
        Preconditions.checkArgument(dc != null);
        if(jpaService != null) {
            Metric metric = getEntityFromModel(metrics);
            jpaService.add(metric);
        }else{
            log.error(metrics.getMetricName() + " could not be saved as the JPA Service is null.");
        }
     }

    /**
     * Store a list of TSDRMetrics.
    */
    @Override
    public void store(List<TSDRRecord> metricList){
        Preconditions.checkArgument(metricList != null);
        if ( metricList != null && metricList.size() != 0){
            for(TSDRRecord metric: metricList){
                store((TSDRMetricRecord)metric);
            }
        }
    }

    @Override public void start(int timeout) {

    }

    @Override public void stop(int timeout) {
        if(jpaService != null) {
            jpaService.close();
            jpaService = null;
        }
    }


    @Override
    public List<TSDRMetricRecord> getTSDRMetricRecords(String metricsCategory, long startDateTime, long endDateTime) {

        if(jpaService != null){
            return jpaService.getMetricsFilteredByCategory(metricsCategory, startDateTime, endDateTime);
        }else{
            log.warn("JPA store service is found to be null in getMetrics");
            return new ArrayList<TSDRMetricRecord>();
        }
    }

    @Override
    public void purgeTSDRRecords(DataCategory category, Long retention_time){
        if(jpaService != null){
            jpaService.purge(category,retention_time);
        }else{
            log.warn("JPA store service is found to be null in purge");
        }
    }
    @Override
    public void purgeAllTSDRRecords(Long retentionTime){
        if(jpaService != null){
            jpaService.purgeAll(retentionTime);
        }
    }
    /**
     * Get persistence entry from TSDRMetric object.
     *
     * @param data
     * @return <code>Metric</code> persitence entity populated
     * @throws IllegalArgumentException if any of preconditions fails
     */
    public Metric getEntityFromModel(TSDRMetricRecord data){
        Preconditions.checkArgument(data != null,"getEntityFromModel found metric data = null");
        Preconditions.checkArgument(data.getNodeID() != null,"getEntityFromModel found metric data nodeId = null" );
        Preconditions.checkArgument(data.getMetricName() != null,"getEntityFromModel found metric name = null" );
        Preconditions.checkArgument(data.getMetricValue() != null,"getEntityFromModel found metric value = null");
        Preconditions.checkArgument(data.getTSDRDataCategory()!=null,"getEntityFromModel found timestamp of metric = null");

        Metric metric = new Metric();


        metric.setNodeId(data.getNodeID());
        metric.setMetricName(data.getMetricName());
        metric.setMetricValue(data.getMetricValue().doubleValue());
        metric.setMetricCategory(data.getTSDRDataCategory().name());
        metric.setMetricTimeStamp(data.getTimeStamp());
        //String detail = FormatUtil.convertToMetricDetailsJSON(FormatUtil.getMetricsDetails(data), data.getTSDRDataCategory().name());
        String detail = FormatUtil.getTSDRMetricKey(data);
        if(null != detail && !detail.isEmpty()) {
            metric.setMetricDetails(detail);
        }

        return metric;
    }


    /**
     * Get persistence entry from TSDRMetric object.
     *
     * @param data
     * @return <code>Metric</code> persitence entity populated
     * @throws IllegalArgumentException if any of preconditions fails
     */
    public Metric getEntityFromModel(TSDRLogRecord data){
        Preconditions.checkArgument(data != null, "getEntityFromModel found metric data = null");
        Preconditions.checkArgument(data.getNodeID() != null, "getEntityFromModel found metric data nodeId = null");
        Preconditions.checkArgument(data.getTSDRDataCategory() != null,
            "getEntityFromModel found timestamp of metric = null");

        Metric metric = new Metric();

        metric.setNodeId(data.getNodeID());
        StringBuffer metricName = new StringBuffer();
        for(RecordAttributes recordAttributes:data.getRecordAttributes()){
            metricName.append(recordAttributes.getName())
                .append("_")
                .append(recordAttributes.getValue());
        }

        if(!metricName.toString().isEmpty()){
            metric.setMetricName(metricName.toString());
        }else{
            metric.setMetricName(data.getTSDRDataCategory().name());
        }

        metric.setMetricValue(0D);//TODO WHAT SHOULD BE VALUE.
        metric.setMetricCategory(data.getTSDRDataCategory().name());
        metric.setMetricTimeStamp(data.getTimeStamp().longValue());
        //String detail = FormatUtil.convertToMetricDetailsJSON(FormatUtil.getMetricsDetails(data), data.getTSDRDataCategory().name());
        String detail = data.getRecordFullText();
        if(null != detail && !detail.isEmpty()) {
            metric.setMetricDetails(detail);
        }

        return metric;
    }

    public  TsdrJpaServiceImpl getJpaService() {
        return jpaService;
    }

    public  void setJpaService(TsdrJpaServiceImpl jpaService) {
        this.jpaService = jpaService;
    }

    @Override
    public void store(TSDRLogRecord logRecord) {
       throw new UnsupportedOperationException("TSDRLogRecord needs to be implmented");
    }

    @Override
    public List<TSDRLogRecord> getTSDRLogRecords(String category, long startTime, long endTime){
        throw new UnsupportedOperationException("log records are not yet supported in h2 data store");
    }
}
