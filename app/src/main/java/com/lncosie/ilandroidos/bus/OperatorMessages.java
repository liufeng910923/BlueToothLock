package com.lncosie.ilandroidos.bus;

/**
 * Created by Administrator on 2015/11/20.
 */
public class OperatorMessages {
    static public class NetworkError {
        public NetworkError() {
        }
    }

    static public class OpDelAuth {
        public byte cmd;
        public byte error;
        public byte type;
        public byte uid;

        public OpDelAuth(byte cmd, byte error, byte type, byte uid) {
            this.cmd = cmd;
            this.error = error;
            this.type = type;
            this.uid = uid;
        }
    }

    static public class OpAddAuth {
        public byte cmd;
        public byte error;
        public byte type;
        public byte uid;

        public OpAddAuth(byte cmd, byte error, byte type, byte uid) {
            this.cmd = cmd;
            this.error = error;
            this.type = type;
            this.uid = uid;
        }
    }
}
