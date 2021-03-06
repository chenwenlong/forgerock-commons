/**
 *
 ~ DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 ~
 ~ Copyright (c) 2011-2013 ForgeRock AS. All Rights Reserved
 ~
 ~ The contents of this file are subject to the terms
 ~ of the Common Development and Distribution License
 ~ (the License). You may not use this file except in
 ~ compliance with the License.
 ~
 ~ You can obtain a copy of the License at
 ~ http://forgerock.org/license/CDDLv1.0.html
 ~ See the License for the specific language governing
 ~ permission and limitations under the License.
 ~
 ~ When distributing Covered Code, include this CDDL
 ~ Header Notice in each file and include the License file
 ~ at http://forgerock.org/license/CDDLv1.0.html
 ~ If applicable, add the following below the CDDL Header,
 ~ with the fields enclosed by brackets [] replaced by
 ~ your own identifying information:
 ~ "Portions Copyrighted [year] [name of copyright owner]"
 *
 */
package org.forgerock.xacml.core.v3.Functions;

/*
urn:oasis:names:tc:xacml:1.0:function:double-multiply
functions SHALL take two arguments of the specified data-type, integer, or double,
and SHALL return an element of integer or double data-type, respectively.
However, the “add” and “multiply” functions MAY take more than two arguments.
Each function evaluation operating on doubles SHALL proceed as specified
by their logical counterparts in IEEE 754 [IEEE754].
For all of these functions, if any argument is "Indeterminate",
then the function SHALL evaluate to "Indeterminate".
In the case of the divide functions, if the divisor is zero, then the function SHALL evaluate to “Indeterminate”.
This function MUST accept two or more arguments.
*/

import org.forgerock.xacml.core.v3.engine.XACML3EntitlementException;
import org.forgerock.xacml.core.v3.engine.XACMLEvalContext;
import org.forgerock.xacml.core.v3.model.DataType;
import org.forgerock.xacml.core.v3.model.DataValue;
import org.forgerock.xacml.core.v3.model.FunctionArgument;
import org.forgerock.xacml.core.v3.model.XACMLFunction;

/**
 * urn:oasis:names:tc:xacml:1.0:function:double-multiply
 */
public class DoubleMultiply extends XACMLFunction {

    public DoubleMultiply()  {
    }

    public FunctionArgument evaluate( XACMLEvalContext pip) throws XACML3EntitlementException {

        if ( getArgCount() < 2) {
            throw new XACML3EntitlementException("Function Requires 2 or more arguments, " +
                    "only "+getArgCount()+" in stack.");
        }
        // Consume are first and second arguments and iterate from their to multiply across all arguments.
        // We have a Valid DataType, Accumulate Arguments, or else Entitlement Exception will be thrown.
        FunctionArgument retVal = new DataValue(DataType.XACMLDOUBLE, (getArg(0).asDouble(pip) * getArg(1).asDouble
                (pip)), true);
        for(int i=2; i<getArgCount(); i++) {
            Double argumentValue = getArg(i).asDouble(pip);
            retVal = new DataValue(DataType.XACMLDOUBLE, (retVal.asDouble(pip) * argumentValue), true);
        }
        // return the Accumulated Value.
        return retVal;
    }
}
