// Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT

package com.amazonaws.jenkins.plugins.sam.util;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.regions.Regions;

import hudson.util.ListBoxModel;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Trek10, Inc.
 */
public class BeanHelper {

    public static ListBoxModel doFillRegionItems() {
        ListBoxModel list = new ListBoxModel();
        for (Region region : RegionUtils.getRegions()) {
            String regionName = region.getName();
            try {
                Regions regionData = Regions.fromName(regionName);
                list.add(regionData.getDescription(), regionData.getName());
            } catch(Exception e) {
                LOGGER.log(Level.INFO, "Failed to enumerate AWS region '" + regionName + "'", e);
            }
        }
        return list;
    }

    private static final Logger LOGGER = Logger.getLogger(BeanHelper.class.getName());
}
