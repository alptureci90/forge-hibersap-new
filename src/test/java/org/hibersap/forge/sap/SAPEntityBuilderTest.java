/*
 * Copyright (C) 2012 akquinet AG
 *
 * This file is part of the Forge Hibersap Plugin.
 *
 * The Forge Hibersap Plugin is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * The Forge Hibersap Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with the Forge Hibersap Plugin. If not, see <http://www.gnu.org/licenses/>.
 */

package org.hibersap.forge.sap;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.CoreMatchers;
import org.hibersap.annotations.Bapi;
import org.hibersap.annotations.Export;
import org.hibersap.annotations.Import;
import org.hibersap.annotations.Parameter;
import org.hibersap.annotations.ParameterType;
import org.hibersap.annotations.Table;
import org.hibersap.mapping.model.BapiMapping;
import org.hibersap.mapping.model.FieldMapping;
import org.hibersap.mapping.model.StructureMapping;
import org.hibersap.mapping.model.TableMapping;
import org.jboss.forge.parser.java.Field;
import org.jboss.forge.parser.java.JavaClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Max Schwaab
 *
 */
public class SAPEntityBuilderTest {

	private static final String CLASS_NAME = "mySAPClass";
	private static final String JAVA_PACKAGE = "myPackage";

	private JavaClass javaClass;

	private BapiMapping createMapping() {
		final BapiMapping mapping = new BapiMapping(null, "BAPI_FLCONN_GETDETAIL", null);

		mapping.addImportParameter(new FieldMapping(String.class, "TRAVELAGENCYNUMBER", "_travelagencynumber", null));
		mapping.addImportParameter(new FieldMapping(String.class, "CONNECTIONNUMBER", "_connectionnumber", null));
		mapping.addImportParameter(new FieldMapping(String.class, "NO_AVAILIBILITY", "_noAvailibility", null));
		mapping.addImportParameter(new FieldMapping(Date.class, "FLIGHTDATE", "_flightdate", null));

		final Set<FieldMapping> priceInfoParamters = new HashSet<FieldMapping>();
		priceInfoParamters.add(new FieldMapping(BigDecimal.class, "PRICE_ECO2", "_priceEco2", null));
		priceInfoParamters.add(new FieldMapping(String.class, "CURR", "_curr", null));
		mapping.addExportParameter(new StructureMapping(null, "PRICE_INFO", "_priceInfo", priceInfoParamters));

		final Set<FieldMapping> extOutParamters = new HashSet<FieldMapping>();
		extOutParamters.add(new FieldMapping(String.class, "STRUCTURE", "_structure", null));
		extOutParamters.add(new FieldMapping(String.class, "VALUEPART4", "_valuepart4", null));
		final StructureMapping structureMapping = new StructureMapping(null, "EXTENSION_OUT", "_extensionOut",
				extOutParamters);
		mapping.addTableParameter(new TableMapping(List.class, null, "EXTENSION_OUT", "_extensionOut", structureMapping));

		return mapping;
	}

	@Before
	public void createSessionManager() {
		final BapiMapping mapping = createMapping();

		final SAPEntityBuilder builder = new SAPEntityBuilder();
		builder.createNew(SAPEntityBuilderTest.CLASS_NAME, SAPEntityBuilderTest.JAVA_PACKAGE, mapping);
		this.javaClass = builder.getSAPEntity().getBapiClass();
	}

	@Test
	public void createsBasicClassDeclarations() {
		Assert.assertThat(this.javaClass.getName(),
				CoreMatchers.is(CoreMatchers.equalTo(SAPEntityBuilderTest.CLASS_NAME)));
		Assert.assertThat(this.javaClass.getPackage(), CoreMatchers.equalTo(SAPEntityBuilderTest.JAVA_PACKAGE));
		Assert.assertThat(this.javaClass.getAnnotations().size(), CoreMatchers.is(1));
		Assert.assertThat(this.javaClass.getAnnotation(Bapi.class), CoreMatchers.is(CoreMatchers.notNullValue()));
	}

	@Test
	public void createsAllFields() {
		final String[] allFieldNames = { "_travelagencynumber", "_noAvailibility", "_connectionnumber", "_flightdate",
				"_priceInfo", "_extensionOut", };

		Assert.assertThat(this.javaClass.getFields().size(), CoreMatchers.is(6));

		for (final String fieldName : allFieldNames) {
			Assert.assertThat(this.javaClass.getField(fieldName), CoreMatchers.notNullValue());
		}
	}

	@Test
	public void createsSimpleImportParameter() {
		final Field<JavaClass> field = this.javaClass.getField("_flightdate");

		Assert.assertThat(field.getType(), CoreMatchers.equalTo("Date"));
		Assert.assertThat(field.getAnnotations().size(), CoreMatchers.is(2));
		Assert.assertThat(field.getAnnotation(Import.class), CoreMatchers.is(CoreMatchers.notNullValue()));
		Assert.assertThat(field.getAnnotation(Parameter.class), CoreMatchers.is(CoreMatchers.notNullValue()));
		Assert.assertThat(field.getAnnotation(Parameter.class).getStringValue("value"),
				CoreMatchers.equalTo("FLIGHTDATE"));
	}

	@Test
	public void createsComplexExportParameter() {
		final Field<JavaClass> field = this.javaClass.getField("_priceInfo");

		Assert.assertThat(field.getType(), CoreMatchers.equalTo("PriceInfo"));
		Assert.assertThat(field.getAnnotations().size(), CoreMatchers.is(2));
		Assert.assertThat(field.getAnnotation(Export.class), CoreMatchers.is(CoreMatchers.notNullValue()));
		Assert.assertThat(field.getAnnotation(Parameter.class), CoreMatchers.is(CoreMatchers.notNullValue()));
		Assert.assertThat(field.getAnnotation(Parameter.class).getStringValue("value"),
				CoreMatchers.equalTo("PRICE_INFO"));
		Assert.assertThat(field.getAnnotation(Parameter.class).getEnumValue(ParameterType.class, "type"),
				CoreMatchers.equalTo(ParameterType.STRUCTURE));
	}

	@Test
	public void createsTableParamater() {
		final Field<JavaClass> field = this.javaClass.getField("_extensionOut");

		Assert.assertThat(field.getType(), CoreMatchers.equalTo("List"));
		Assert.assertThat(field.getAnnotations().size(), CoreMatchers.is(2));
		Assert.assertThat(field.getAnnotation(Table.class), CoreMatchers.is(CoreMatchers.notNullValue()));
		Assert.assertThat(field.getAnnotation(Parameter.class), CoreMatchers.is(CoreMatchers.notNullValue()));
		Assert.assertThat(field.getAnnotation(Parameter.class).getStringValue("value"),
				CoreMatchers.equalTo("EXTENSION_OUT"));
	}
}