package naito_rescue.message.converter.compressor;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import static naito_rescue.debug.DebugUtil.*;
import naito_rescue.message.*;
import naito_rescue.message.converter.*;
import naito_rescue.message.stream.*;
import naito_rescue.agent.*;

import rescuecore2.messages.*;
import rescuecore2.worldmodel.*;
import rescuecore2.Constants;
import rescuecore2.standard.components.*;
import rescuecore2.standard.entities.*;
import rescuecore2.standard.messages.*;
import rescuecore2.misc.geometry.*;

public interface ICompressorModule
{
	public RawDataInputStream decompress(byte[] rawdata);
	
	public byte[] compress(RawDataOutputStream stream);
}
