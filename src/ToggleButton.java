// Class ToggleButton - a toggling button component
// 
// Author:
//   Jussi Hynninen <hynde@iki.fi>
// Date:
//   ** May 1996

import java.awt.*;

class ToggleButton extends Button {

  private boolean state = false;

  private String statef = "False", statet = "True";

  public ToggleButton(String statef, String statet) {
    super(statef);
    this.statef = statef;
    this.statet = statet;
  }

  public ToggleButton(String statef, String statet, boolean initstate) {
    super(initstate ? statet : statef);
    this.statef = statef;
    this.statet = statet;
    state = initstate;
  }

  public boolean toggleState() {
    state = !state;
    setLabel(state ? statet : statef);
    return state;
  }

  public boolean getState() { return state; }

  public void setState(boolean state) {
    this.state = state;
    setLabel(this.state ? statet : statef);
  }

  public void setLabels(String statef, String statet) {
    this.statef = statef;
    this.statet = statet;
    setLabel(state ? statet : statef);
  }
  
}
