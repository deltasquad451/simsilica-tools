/*
 * $Id$
 * 
 * Copyright (c) 2014, Simsilica, LLC
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the 
 *    distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
package com.simsilica.arboreal;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Checkbox;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.RollupPanel;
import com.simsilica.lemur.TabbedPanel;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.event.BaseAppState;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.style.ElementId;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 *  @author    Paul Speed
 */
public class TreeOptionsState extends BaseAppState {

    static Logger log = LoggerFactory.getLogger(TreeOptionsState.class);
    
    private Container mainWindow;
    private Container mainContents; 
 
    private Container viewSettingsPanel;
    private Container actionsPanel;
    private Container checkboxPanel;
    private TabbedPanel tabs;
 
    private List<Checkbox> optionToggles = new ArrayList<Checkbox>();
    private Map<String, Checkbox> optionToggleMap = new HashMap<String, Checkbox>();

    private int columns = 3;
    
    public TreeOptionsState() {
    }

    public Container getContents() {
        return mainContents;
    }
 
    public Container getViewSettings() {
        return viewSettingsPanel;
    }
 
    public TabbedPanel getParameterTabs() {
        return tabs;
    }
 
    public Checkbox addOptionToggle( String name, Object target, String method ) {
        Checkbox cb = new Checkbox(name, "glass");
        cb.addClickCommands(new ToggleHandler(target, method));
                        
        int column = optionToggles.size() % columns;
        if( checkboxPanel != null ) {
            if( column == 0 ) {
                checkboxPanel.addChild(cb);
            } else {
                checkboxPanel.addChild(cb, column);
            } 
        }
        optionToggles.add(cb);
        optionToggleMap.put(name, cb);
        return cb;        
    }
    
    public void toggleHud() {
        setEnabled( !isEnabled() );
    }

    @Override
    protected void initialize( Application app ) {
    
        // Always register for our hot key as long as
        // we are attached.
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.addDelegate( MainFunctions.F_HUD, this, "toggleHud" );
                
        mainWindow = new Container(new BorderLayout(), new ElementId("window"), "glass");
        //mainWindow.addChild(new Label("Tree Options", mainWindow.getElementId().child("title.label"), "glass"),
        //                   BorderLayout.Position.North); 
        mainWindow.setLocalTranslation(10, app.getCamera().getHeight() - 10, 0);        
        
        mainContents = mainWindow.addChild(new Container(mainWindow.getElementId().child("contents.container"), "glass"),
                                                        BorderLayout.Position.Center); 
               
        //mainContents.addChild(new Label("Visualization Options:", "glass"));
        actionsPanel = mainContents.addChild(new Container());
 
        Container visOptions = new Container();       
        RollupPanel visRollup = new RollupPanel("Visualization Options", visOptions, 
                                                new ElementId("root.rollup"), "glass");
        mainContents.addChild(visRollup);
        viewSettingsPanel = visOptions.addChild(new Container());
        checkboxPanel = visOptions.addChild(new Container());
        
        // Add any toggles that were added before init
        int i = 0;
        for( Checkbox cb : optionToggles ) {
            int column = (i++) % columns;
            if( column == 0 ) {
                checkboxPanel.addChild(cb);
            } else {
                checkboxPanel.addChild(cb, column);
            } 
        }
         
        //mainContents.addChild(new Label("Tree Parameters:", "glass"));
        //tabs = mainContents.addChild(new TabbedPanel("glass"));
        tabs = new TabbedPanel("glass");
        mainContents.addChild(new RollupPanel("Tree Parameters", tabs, 
                                               new ElementId("root.rollup"), "glass"));
    }

    @Override
    protected void cleanup( Application app ) {
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.removeDelegate( MainFunctions.F_HUD, this, "toggleHud" ); 
    }

    @Override
    protected void enable() {
        ((SimpleApplication)getApplication()).getGuiNode().attachChild(mainWindow);
    }
    
    @Override
    protected void disable() {
        mainWindow.removeFromParent();
    }
    
    private class ToggleHandler implements Command<Button> {
        private Object object;
        private Method method;
        
        public ToggleHandler( Object object, String methodName ) {
            this.object = object;
            try {
                this.method = object.getClass().getMethod(methodName, Boolean.TYPE);
            } catch( Exception e ) {
                throw new RuntimeException("Error retrieving method for:" + methodName, e);
            }
        }
        
        @Override
        public void execute( Button source ) {
            try {
                method.invoke(object, ((Checkbox)source).isChecked());
            } catch( Exception e ) {
                throw new RuntimeException("Error sending state for:" + object + "->" + method, e);
            }
        } 
    }
}
