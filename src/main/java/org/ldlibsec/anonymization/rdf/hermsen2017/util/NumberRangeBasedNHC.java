package org.ldlibsec.anonymization.rdf.hermsen2017.util;

import org.ldlibsec.anonymization.rdf.evaluation.AttributeCount;

import java.util.List;

/**
 * Assuming that Attribute Count values are numbers and thus can be ranged to Integer ranges
 * If you want double ranges like (0.1-0.4 and similar)  please use the @{DoubleRangeBasedNHC}
 *
 */
public class NumberRangeBasedNHC extends NHC {



    public NumberRangeBasedNHC(List<AttributeCount> attributes){
        super(attributes);
    }


    @Override
    public String code() {
        return null;
    }
}
