package cn.sdaict.fldatauploader.utils;

import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

public class NullRepresenter extends Representer {
    public NullRepresenter() {
        super();
        this.nullRepresenter = new RepresentNull();
    }

    private class RepresentNull implements Represent {
        public Node representData(Object data) {
            return representScalar(Tag.NULL, "");
        }
    }
}

