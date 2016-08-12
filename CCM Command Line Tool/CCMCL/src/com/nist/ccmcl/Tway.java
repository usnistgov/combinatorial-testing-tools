package com.nist.ccmcl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.locks.ReentrantLock;

public class Tway
        extends RecursiveTask
{
    public static  ReentrantLock                              lock            = new ReentrantLock();
    /*
     * Multi-deminisional arrays to hold used combinations...
     */
    private static ConcurrentHashMap<String, int[][]>         comcount_array2 = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, int[][][]>       comcount_array3 = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, int[][][][]>     comcount_array4 = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, int[][][][][]>   comcount_array5 = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, int[][][][][][]> comcount_array6 = new ConcurrentHashMap<>();
    private static List<Parameter>    _parameters;
    private static List<meConstraint> _constraints;
    public         double             div;
    public         int                nComs;
    public         int                varvaltotal;
    public          double  sumcov      = 0;
    public volatile boolean initialized = false;
    public int[][] hm_colors2;
    public long    _n_tot_tway_cov;
    public long[]  _varvalStatN;
    public long    _nComs;
    public long    _tot_varvalconfig;
    public long    _invalidcombNotCovered;
    public List<String[][]> _aInvalidComb  = null;
    public List<String[][]> _aInvalidNotIn = null;
    public String           appendFile     = "";
    int[][] out_test;
    int     Nout_test;
    private String  _tway;
    private int     _start;
    private int     _end;
    private boolean _parallel;
    // JProgressBar _barra;
    private int     _ncols;
    private int     _nrows;
    private int[][] _test;
    private int[]   _nvals;
    private String _fileNameMissing = null;
    private String _fileNameReport  = null;
    private int        _NGenTests;
    private boolean    _GenTests;
    private double     _minCov;
    private String[][] _map;
    private boolean    _rptMissingCom;
    private int     _MaxGenTests = 10000;
    private boolean _appendTests = false;
    private Boolean[]  _rng;
    private double[][] _bnd;
    private Boolean[]  _grp;
    private Object[][] _group;
    private boolean     wait      = false;
    private int         _parmName = 0;
    private List<int[]> _missing  = new ArrayList<>();
    private int         NBINS     = 20;

    public Tway(String tway, int start, int end, int[][] test, int[] nvals, int nrows, int ncols,
                List<Parameter> parameters, List<meConstraint> constraints, String[][] map)
    {
        _tway = tway;
        _start = start;
        _end = end;
        _test = test;
        _nvals = nvals;
        _nrows = nrows;
        _ncols = ncols;
        _parameters = parameters;
        _constraints = constraints;
        _map = map;
    }

    // set properties

    public static synchronized void write(String sFileName, String sContent)
    {
        try
        {

            File oFile = new File(sFileName);
            if (!oFile.exists())
            {
                oFile.createNewFile();
            }
            if (oFile.canWrite())
            {
                BufferedWriter oWriter = new BufferedWriter(new FileWriter(sFileName, true));
                oWriter.append(sContent);
                oWriter.newLine();
                oWriter.flush();
                oWriter.close();
            }

        }
        catch (IOException oException)
        {
            throw new IllegalArgumentException("Error appending/File cannot be written: \n" + sFileName);
        }
    }

    public static synchronized void print_progress()
    {
        double progress[] = new double[5];
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\nMEASUREMENT PROGRESS: \n\n");
        for (int i = 0; i < 5; i++)
        {
            progress[i] = ((double) Main.progress[i] / ((double) Main.ncols - (i + 1)) * 100);
            if (Main.tway_objects[i] != null)
            {
                System.out.println(String.format("%dway progress: %.2f%%", i + 2, progress[i]));
            }
        }

    }

    public void set_wait(boolean w)
    {
        wait = w;
    }

    public void set_appendFile(String x)
    {
        appendFile = x;
    }

    public void set_parmName(int p)
    {
        _parmName = p;
    }

    public void set_map(String[][] m)
    {
        _map = m;
    }

    public void set_bnd(double[][] d)
    {
        _bnd = d;
    }

    public void set_Rng(Boolean b[])
    {
        _rng = b;
    }

    public void set_grp(Boolean b[])
    {
        _grp = b;
    }

    public void set_group(Object[][] d)
    {
        _group = d;
    }

    public void set_appendTests(boolean b)
    {
        _appendTests = b;
    }

    public void set_MaxGenTests(int i)
    {
        _MaxGenTests = i;
    }

    public void set_minCov(double d)
    {
        _minCov = d;
    }

    public void set_rptMissingCom(boolean m)
    {
        _rptMissingCom = m;
    }

    public void set_GenTests(boolean b)
    {
        _GenTests = b;
    }

    public void set_NGenTests(int i)
    {
        _NGenTests = i;

    }

    public void set_FileNameMissing(String fn)
    {
        _fileNameMissing = fn;

    }

    public void set_FileNameReport(String fn)
    {
        _fileNameReport = fn;

    }

    public void set_Parallel(boolean b)
    {
        _parallel = b;
    }

    public List<String[][]> get_InvalidComb()
    {
        return _aInvalidComb;
    }

    public List<String[][]> get_InvalidNotin()
    {

        return _aInvalidNotIn;
    }

    // methods
    private void GetTway()
    {
        switch (_tway)
        {
            case "2way":
                TwoWay();
                break;
            case "3way":
                ThreeWay();
                break;
            case "4way":
                FourWay();
                break;
            case "5way":
                FiveWay();
                break;
            case "6way":
                SixWay();
                break;
        }

    }

    private void TwoWay()
    {
        //used to store the used 2way combinations.
        //used2way = new HashMap();

        long n_tot_tway_cov;
        long tot_varvalconfigs2;
        int i, j, ni, nj, m, ti;
        long[] varvalStats2 = new long[NBINS + 1];
        long n_varvalconfigs2;

        nComs = 0; // number of combinations = C(ncols, t)
        varvaltotal = 0;
        n_tot_tway_cov = 0;
        tot_varvalconfigs2 = 0;
        sumcov = 0;

        hm_colors2 = new int[_ncols][];
        for (i = 0; i < _ncols; i++)
        {
            hm_colors2[i] = new int[_ncols];
        }

        for (i = 0; i < NBINS + 1; i++)
        {
            varvalStats2[i] = 0;
        }

        for (i = _start; i < _end; i++)
        {
            for (j = i + 1; j < _ncols; j++)
            {
                nComs++;
                varvaltotal += (_nvals[i] * _nvals[j]);
            }
        }

        div = (double) varvaltotal / (double) nComs;

        _aInvalidComb = new ArrayList<>();
        _aInvalidNotIn = new ArrayList<>();


        // solver for invalid combinations
        CSolver validcomb = new CSolver();
        validcomb.SetConstraints(_constraints);
        validcomb.SetParameter(_parameters);


		/*
         * -Calculates the number of t-way combinations between parameters
		 *
		 * -Calculates the total variable value configurations
		 */

        // Process the tests

        for (i = _start; i < _end; i++)
        {
            for (j = i + 1; j < _ncols; j++)
            {
                int[][] comcount = new int[_nvals[i]][];
                for (ti = 0; ti < _nvals[i]; ti++)
                {
                    comcount[ti] = new int[_nvals[j]];
                    for (int zz = 0; zz < _nvals[j]; zz++)
                    {
                        comcount[ti][zz] = 0;
                    }
                }
//                int[][] comcount = new int[_nvals[i]][];
//                for (ti = 0; ti < _nvals[i]; ti++)
//                {
//                    comcount[ti] = new int[_nvals[j]];
//                }


                String temp_key = String.format("%s(%d,%d)", _tway, i, j);


                // forall t-way combinations of input variable values:
                // comcount i,j == 0
                // for the combination designated by i,j increment counts
                for (m = 0; m < _nrows; m++)
                {
                    int v1 = _test[m][i];
                    int v2 = _test[m][j];
                    String[][] pars = new String[2][];
                    pars[0] = new String[2];
                    pars[1] = new String[2];

                    if (i >= _parameters.size() ||
                        j >= _parameters.size())
                    {
                        continue;
                    }
                    pars[0][0] = _parameters.get(i).getName();
                    pars[1][0] = _parameters.get(j).getName();

                    if (v1 >= _parameters.get(i).getValues().size() ||
                        v2 >= _parameters.get(j).getValues().size())
                    {
                        continue;
                    }
                    pars[0][1] = _parameters.get(i).getValues().get(v1);
                    pars[1][1] = _parameters.get(j).getValues().get(v2);

                    if (v1 < comcount.length)
                    {
                        if (v2 < comcount[v1].length)
                        {
                            if (_constraints.size() > 0)
                            {
                                if (validcomb.EvaluateCombination(pars))
                                {
                                    comcount[v1][v2] = 1; // flag valid var-val config in set test
                                } else
                                {
                                    comcount[v1][v2] = -1; // flag invalid var-val config in set test
                                }
                            } else
                            {
                                comcount[v1][v2] = 1; // flag var-val config in set test
                            }
                        }
                    }

                    // coumcount i,j == 1 iff some tests contains tuple i,j
                }

                int varval_cnt = 0;
                int invalidcomb = 0;
                int invalidcombNotCovered = 0;

                for (ni = 0; ni < _nvals[i]; ni++)
                {
                    for (nj = 0; nj < _nvals[j]; nj++)
                    {
                        // count how many value var-val configs are contained in
                        // a test
                        if (comcount[ni][nj] == 1)
                        {
                            varval_cnt++;
                            n_tot_tway_cov++;
                        } else
                        {
                            String[][] pars = new String[2][];
                            pars[0] = new String[2];
                            pars[1] = new String[2];

                            if (i >= _parameters.size() || j >= _parameters.size())
                            {
                                continue;
                            }
                            pars[0][0] = _parameters.get(i).getName();
                            pars[1][0] = _parameters.get(j).getName();

                            if (ni >= _parameters.get(i).getValues().size() ||
                                nj >= _parameters.get(j).getValues().size())
                            {
                                continue;
                            }
                            pars[0][1] = _parameters.get(i).getValues().get(ni);
                            pars[1][1] = _parameters.get(j).getValues().get(nj);

                            // count how many invalid configs are contained in
                            // the test
                            if (comcount[ni][nj] == -1)
                            {
                                invalidcomb += 1;
                                _aInvalidComb.add(pars);
                            }
                            if (comcount[ni][nj] == 0 && _constraints.size() > 0 && _nrows > 0)
                            {
                                if (!validcomb.EvaluateCombination(pars))
                                {
                                    // count how many invalid configs are not
                                    // contained in the test
                                    invalidcombNotCovered += 1;
                                    comcount[ni][nj] = -3;
                                    _aInvalidNotIn.add(pars);
                                }
                            }
                        }
                    }
                }
                //Store this comcount array for real time measurement...
                comcount_array2.put(temp_key, comcount);

                n_varvalconfigs2 = _nvals[i] * _nvals[j];
                n_varvalconfigs2 -= (invalidcomb + invalidcombNotCovered);
                tot_varvalconfigs2 += n_varvalconfigs2;

                double varval_cov = (double) varval_cnt / (double) n_varvalconfigs2;

                double varval = (double) varval_cnt / (double) varvaltotal;

                sumcov += varval;
                // sumcov+=(double)varval_cnt/(double)varvaltotal;
                // varval_cov bins give the number of var/val configurations
                // covered
                // at the levels: 0, [5,10), [10,15) etc. (assume 20 bins)
                // (double)Math.round(value * 100000) / 100000

                for (int b = 0; b <= NBINS; b++)
                // if (((double)Math.round(varval*nComs*10)/10)>=
                // (double)((double)b / (double)NBINS)) varvalStats2[b]++;
                {
                    if (varval_cov >= (double) b / (double) NBINS)
                    {
                        varvalStats2[b]++;
                    }
                }

                // now determine color for heat map display
                if (varval_cov < 0.2)
                {
                    hm_colors2[i][j] = 0;
                } else if (varval_cov < 0.4)
                {
                    hm_colors2[i][j] = 1;
                } else if (varval_cov < 0.6)
                {
                    hm_colors2[i][j] = 2;
                } else if (varval_cov < 0.8)
                {
                    hm_colors2[i][j] = 3;
                } else
                {
                    hm_colors2[i][j] = 4;
                }

                // now write out missing combinations in format for compressing
                // into tests later
                // if (varval_cov < min_cov)
                // for ki = i and kj = j, if comcount[ki][kj] = 0,
                // for each k in 0..ncols-1 write out '-' if k != i and k != j,
                // otherwise
                // write ki and kj
                // end result
                // - - - 1 - - 0 - - - -
                // - - - 2 - - 4 - - - - etc., showing uncovered vals for each
                // combination
                // that has less than minimum coverage
                if (_GenTests && varval_cov < _minCov)
                {
                    for (ni = 0; ni < _nvals[i]; ni++)
                    {
                        for (nj = 0; nj < _nvals[j]; nj++)
                        {
                            if (comcount[ni][nj] == 0)
                            { // not covered; write
                                // out combination
                                // to cover
                                if (_rptMissingCom)
                                {
                                    String outl = String.format(
                                                                       "%d,%d = %d,%d || %s,%s\n",
                                                                       i, j,
                                                                       ni, nj,
                                                                       _map[i][ni], _map[j][nj]
                                                               );
                                    //String outl = i + "," + j + " = " + ni + "," + nj + " ||" + _map[i][ni] + ","
                                    //              + _map[j][nj] + "\n";
                                    write(_fileNameReport, outl);
                                }

                                int[] im = new int[4];

                                im[0] = i;
                                im[1] = j;
                                im[2] = ni;
                                im[3] = nj;
                                _missing.add(im);

                            }
                        }
                    }
                }
            }

            if (Main.display_progress)
            {
                Main.increment_progress(0);
                print_progress();
            }
        }
        _n_tot_tway_cov = n_tot_tway_cov;
        _nComs = nComs;
        _tot_varvalconfig = tot_varvalconfigs2;
        _varvalStatN = varvalStats2;
        initialized = true;
    }

    private void ThreeWay()
    {
        long n_tot_tway_cov = 0; // drk121109
        int i, j, k, ni, nj, nk, m, ti, tj;
        long[] varvalStats3 = new long[NBINS + 1];
        long n_varvalconfigs3; // drk121109
        long nComs = 0; // number of combinations = C(ncols, t) //drk121109
        long tot_varvalconfigs3 = 0;


        for (i = 0; i < NBINS + 1; i++)
        {
            varvalStats3[i] = 0;
        }

        // solver for invalid combinations
        CSolver validcomb = new CSolver();
        validcomb.SetConstraints(_constraints);
        validcomb.SetParameter(_parameters);

        _aInvalidComb = new ArrayList<>();
        _aInvalidNotIn = new ArrayList<>();

        for (i = _start; i < _end; i++)
        {
            for (j = i + 1; j < _ncols - 1; j++)
            {
                for (k = j + 1; k < _ncols; k++)
                {
                    nComs++;
                    int[][][] comcount = new int[_nvals[i]][][]; // allow row 0
                    // for # values per parm
                    for (ti = 0; ti < _nvals[i]; ti++)
                    {
                        comcount[ti] = new int[_nvals[j]][];
                        for (tj = 0; tj < _nvals[j]; tj++)
                        {
                            comcount[ti][tj] = new int[_nvals[k]];
                        }
                    }
                    String temp_key = String.format("%s(%d,%d,%d)", _tway, i, j, k);


                    // forall t-way combinations of input variable values:

                    // comcount i,j == 0
                    // for the combination designated by i,j increment counts
                    for (m = 0; m < _nrows; m++)
                    { // mark if the var-val config
                        // is covered by the tests

                        int v1 = _test[m][i];
                        int v2 = _test[m][j];
                        int v3 = _test[m][k];
                        String[][] pars = new String[3][];
                        pars[0] = new String[2];
                        pars[1] = new String[2];
                        pars[2] = new String[2];

                        if (i >= _parameters.size() ||
                            j >= _parameters.size() ||
                            k >= _parameters.size())
                        {
                            continue;
                        }
                        pars[0][0] = _parameters.get(i).getName();
                        pars[1][0] = _parameters.get(j).getName();
                        pars[2][0] = _parameters.get(k).getName();

                        if (v1 >= _parameters.get(i).getValues().size() ||
                            v2 >= _parameters.get(j).getValues().size() ||
                            v3 >= _parameters.get(k).getValues().size())
                        {
                            continue;
                        }
                        pars[0][1] = _parameters.get(i).getValues().get(v1);
                        pars[1][1] = _parameters.get(j).getValues().get(v2);
                        pars[2][1] = _parameters.get(k).getValues().get(v3);

                        if (v1 < comcount.length)
                        {
                            if (v2 < comcount[v1].length)
                            {
                                if (v3 < comcount[v1][v2].length)
                                {
                                    if (_constraints.size() > 0)
                                    {
                                        if (validcomb.EvaluateCombination(pars))
                                        {
                                            comcount[v1][v2][v3] = 1;
                                        } else
                                        {
                                            comcount[v1][v2][v3] = -1;
                                        }
                                    } else
                                    {
                                        comcount[v1][v2][v3] = 1;
                                    }
                                }
                            }
                        }
                        // coumcount i,j == 1 iff some tests contains tuple i,j
                    }

                    int varval_cnt = 0;
                    int invalidComb = 0;
                    int invalidcombNotCovered = 0;

                    for (ni = 0; ni < _nvals[i]; ni++)
                    {
                        for (nj = 0; nj < _nvals[j]; nj++)
                        {
                            for (nk = 0; nk < _nvals[k]; nk++)
                            {
                                if (comcount[ni][nj][nk] > 0)
                                {
                                    varval_cnt++;
                                    n_tot_tway_cov++;
                                } // count valid configs in set test
                                else
                                {
                                    String[][] pars = new String[3][];
                                    pars[0] = new String[2];
                                    pars[1] = new String[2];
                                    pars[2] = new String[2];

                                    if (i >= _parameters.size() ||
                                        j >= _parameters.size() ||
                                        k >= _parameters.size())
                                    {
                                        continue;
                                    }
                                    pars[0][0] = _parameters.get(i).getName();
                                    pars[1][0] = _parameters.get(j).getName();
                                    pars[2][0] = _parameters.get(k).getName();

                                    if (ni >= _parameters.get(i).getValues().size() ||
                                        nj >= _parameters.get(j).getValues().size() ||
                                        nk >= _parameters.get(k).getValues().size())
                                    {
                                        continue;
                                    }
                                    pars[0][1] = _parameters.get(i).getValues().get(ni);
                                    pars[1][1] = _parameters.get(j).getValues().get(nj);
                                    pars[2][1] = _parameters.get(k).getValues().get(nk);

                                    if (comcount[ni][nj][nk] == -1)
                                    {
                                        // count invalid configs in set test
                                        invalidComb += 1;
                                        _aInvalidComb.add(pars);
                                    }
                                    if (comcount[ni][nj][nk] == 0 && _constraints.size() > 0 && _nrows > 0)
                                    {
                                        if (!validcomb.EvaluateCombination(pars))
                                        {
                                            // count invalid configs not in set test
                                            invalidcombNotCovered += 1;
                                            _aInvalidNotIn.add(pars);
                                            comcount[ni][nj][nk] = -3;
                                        }

                                    }
                                }
                            }
                        }
                    }

                    comcount_array3.put(temp_key, comcount);

                    n_varvalconfigs3 = _nvals[i] * _nvals[j] * _nvals[k];
                    n_varvalconfigs3 -= (invalidComb + invalidcombNotCovered);
                    tot_varvalconfigs3 += n_varvalconfigs3;

                    double varval_cov = (double) varval_cnt / (double) n_varvalconfigs3;
                    // varval_cov bins give the number of var/val configurations
                    // covered at the levels: 0, [5,10), [10,15) etc. (assume 20 bins)
                    for (int b = 0; b <= NBINS; b++)
                    {
                        if (varval_cov >= (double) b / (double) NBINS)
                        {
                            varvalStats3[b]++;
                        }
                    }


                    // For missing combinations

                    if (_GenTests && varval_cov < _minCov)
                    {
                        for (ni = 0; ni < _nvals[i]; ni++)
                        {
                            for (nj = 0; nj < _nvals[j]; nj++)
                            {
                                for (nk = 0; nk < _nvals[k]; nk++)
                                {
                                    if (comcount[ni][nj][nk] == 0)
                                    {
                                        // not covered;  write out combination to cover
                                        if (_rptMissingCom)
                                        {

                                            String outl = String.format(
                                                                               "%d,%d,%d = %d,%d,%d || %s,%s,%s\n",
                                                                               i, j, k,
                                                                               ni, nj, nk,
                                                                               _map[i][ni], _map[j][nj], _map[k][nk]
                                                                       );
                                            //String outl = i + "," + j + "," + k + " = " + ni + "," + nj + "," + nk
                                            //              + " ||" + _map[i][ni] + "," + _map[j][nj] + "," + _map[k][nk]
                                            //              + "\n";
                                            write(_fileNameReport, outl);
                                        }

                                        int[] im = new int[6];

                                        im[0] = i;
                                        im[1] = j;
                                        im[2] = k;
                                        im[3] = ni;
                                        im[4] = nj;
                                        im[5] = nk;
                                        _missing.add(im);

                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (Main.display_progress)
            {
                Main.increment_progress(1);
                print_progress();
            }

        }
        _n_tot_tway_cov = n_tot_tway_cov;
        _nComs = nComs;
        _tot_varvalconfig = tot_varvalconfigs3;
        _varvalStatN = varvalStats3;
        initialized = true;
    }

    private void FourWay()
    {
        long n_tot_tway_cov = 0; // drk121109
        int i, j, k, r, ni, nj, nk, nr, m, ti, tj, tk;
        long[] varvalStats4 = new long[NBINS + 1];
        long n_varvalconfigs4;
        long nComs = 0; // number of combinations = C(ncols, t) //drk121109
        long tot_varvalconfigs4 = 0; // drk121109

        // solver for invalid combinations
        CSolver validcomb = new CSolver();
        validcomb.SetConstraints(_constraints);
        validcomb.SetParameter(_parameters);

        _aInvalidComb = new ArrayList<>();
        _aInvalidNotIn = new ArrayList<>();

        for (i = 0; i < NBINS + 1; i++)
        {
            varvalStats4[i] = 0;
        }

        for (i = _start; i < _end; i++)
        {
            for (j = i + 1; j < _ncols - 2; j++)
            {
                for (k = j + 1; k < _ncols - 1; k++)
                {
                    for (r = k + 1; r < _ncols; r++)
                    {
                        nComs++;
                        int[][][][] comcount = new int[_nvals[i]][][][]; // allow
                        // row 0 for # values per parm
                        for (ti = 0; ti < _nvals[i]; ti++)
                        {
                            comcount[ti] = new int[_nvals[j]][][];
                            for (tj = 0; tj < _nvals[j]; tj++)
                            {
                                comcount[ti][tj] = new int[_nvals[k]][];
                                for (tk = 0; tk < _nvals[k]; tk++)
                                {
                                    comcount[ti][tj][tk] = new int[_nvals[r]];
                                }
                            }
                        }
                        String temp_key = String.format("%s(%d,%d,%d,%d)", _tway, i, j, k, r);

                        // forall t-way combinations of input variable values:
                        // comcount i,j == 0
                        // for the combination designated by i,j increment
                        // counts
                        for (m = 0; m < _nrows; m++)
                        {
                            int v1 = _test[m][i];
                            int v2 = _test[m][j];
                            int v3 = _test[m][k];
                            int v4 = _test[m][r];
                            String[][] pars = new String[4][];
                            pars[0] = new String[2];
                            pars[1] = new String[2];
                            pars[2] = new String[2];
                            pars[3] = new String[2];

                            if (i >= _parameters.size() ||
                                j >= _parameters.size() ||
                                k >= _parameters.size() ||
                                r >= _parameters.size())
                            {
                                continue;
                            }
                            pars[0][0] = _parameters.get(i).getName();
                            pars[1][0] = _parameters.get(j).getName();
                            pars[2][0] = _parameters.get(k).getName();
                            pars[3][0] = _parameters.get(r).getName();

                            if (v1 >= _parameters.get(i).getValues().size() ||
                                v2 >= _parameters.get(j).getValues().size() ||
                                v3 >= _parameters.get(k).getValues().size() ||
                                v4 >= _parameters.get(r).getValues().size())
                            {
                                continue;
                            }
                            pars[0][1] = _parameters.get(i).getValues().get(v1);
                            pars[1][1] = _parameters.get(j).getValues().get(v2);
                            pars[2][1] = _parameters.get(k).getValues().get(v3);
                            pars[3][1] = _parameters.get(r).getValues().get(v4);
                            if (v1 < comcount.length)
                            {
                                if (v2 < comcount[v1].length)
                                {
                                    if (v3 < comcount[v1][v2].length)
                                    {
                                        if (v4 < comcount[v1][v2][v3].length)
                                        {
                                            if (_constraints.size() > 0)
                                            {
                                                if (validcomb.EvaluateCombination(pars))
                                                {
                                                    comcount[v1][v2][v3][v4] = 1;
                                                } else
                                                {
                                                    comcount[v1][v2][v3][v4] = -1;
                                                }

                                            } else
                                            {
                                                comcount[v1][v2][v3][v4] = 1;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        int varval_cnt = 0;
                        int invalidComb = 0;
                        int invalidcombNotCovered = 0;
                        // count how many value configs are contained in a test
                        for (ni = 0; ni < _nvals[i]; ni++)
                        {
                            for (nj = 0; nj < _nvals[j]; nj++)
                            {
                                for (nk = 0; nk < _nvals[k]; nk++)
                                {
                                    for (nr = 0; nr < _nvals[r]; nr++)
                                    {
                                        if (comcount[ni][nj][nk][nr] == 1)
                                        {
                                            varval_cnt++;
                                            n_tot_tway_cov++;
                                        } else
                                        {
                                            String[][] pars = new String[4][];
                                            pars[0] = new String[2];
                                            pars[1] = new String[2];
                                            pars[2] = new String[2];
                                            pars[3] = new String[2];

                                            if (i >= _parameters.size() ||
                                                j >= _parameters.size() ||
                                                k >= _parameters.size() ||
                                                r >= _parameters.size())
                                            {
                                                continue;
                                            }
                                            pars[0][0] = _parameters.get(i).getName();
                                            pars[1][0] = _parameters.get(j).getName();
                                            pars[2][0] = _parameters.get(k).getName();
                                            pars[3][0] = _parameters.get(r).getName();

                                            if (ni >= _parameters.get(i).getValues().size() ||
                                                nj >= _parameters.get(j).getValues().size() ||
                                                nk >= _parameters.get(k).getValues().size() ||
                                                nr >= _parameters.get(r).getValues().size())
                                            {
                                                continue;
                                            }
                                            pars[0][1] = _parameters.get(i).getValues().get(ni);
                                            pars[1][1] = _parameters.get(j).getValues().get(nj);
                                            pars[2][1] = _parameters.get(k).getValues().get(nk);
                                            pars[3][1] = _parameters.get(r).getValues().get(nr);

                                            if (comcount[ni][nj][nk][nr] == -1)
                                            {
                                                invalidComb += 1;
                                                _aInvalidComb.add(pars);
                                            }

                                            if (comcount[ni][nj][nk][nr] == 0 && _constraints.size() > 0 && _nrows > 0)
                                            {
                                                if (!validcomb.EvaluateCombination(pars))
                                                {
                                                    invalidcombNotCovered += 1;
                                                    _aInvalidNotIn.add(pars);
                                                    comcount[ni][nj][nk][nr] = -3;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        comcount_array4.put(temp_key, comcount);
                        n_varvalconfigs4 = _nvals[i] * _nvals[j] * _nvals[k] * _nvals[r];
                        n_varvalconfigs4 -= (invalidComb + invalidcombNotCovered);
                        tot_varvalconfigs4 += n_varvalconfigs4;

                        double varval_cov = (double) varval_cnt / (double) n_varvalconfigs4;
                        // varval_cov bins give the number of var/val
                        // configurations covered
                        // at the levels: 0, [5,10), [10,15) etc. (assume 20
                        // bins)
                        for (int b = 0; b <= NBINS; b++)
                        {
                            if (varval_cov >= (double) b / (double) NBINS)
                            {
                                varvalStats4[b]++;
                            }
                        }

                        // *********** For missing combinations
                        if (_GenTests && varval_cov < _minCov)
                        {
                            for (ni = 0; ni < _nvals[i]; ni++)
                            {
                                for (nj = 0; nj < _nvals[j]; nj++)
                                {
                                    for (nk = 0; nk < _nvals[k]; nk++)
                                    {
                                        for (nr = 0; nr < _nvals[r]; nr++)
                                        {
                                            if (comcount[ni][nj][nk][nr] == 0)
                                            {
                                                // not covered; write out combination to cover
                                                if (_rptMissingCom)
                                                {
                                                    String outl = String.format(
                                                                                       "%d,%d,%d,%d = %d,%d,%d,%d || %s,%s,%s,%s\n",
                                                                                       i,
                                                                                       j,
                                                                                       k,
                                                                                       r,
                                                                                       ni,
                                                                                       nj,
                                                                                       nk,
                                                                                       nr,
                                                                                       _map[i][ni],
                                                                                       _map[i][ni],
                                                                                       _map[k][nk],
                                                                                       _map[r][nr]
                                                                               );
//                                                    String outl = i + "," + j + "," + k + "," + r + " = " + ni + ","
//                                                                  + nj + "," + nk + "," + nr + " ||" + _map[i][ni] + ","
//                                                                  + _map[i][ni] + "," + _map[k][nk] + "," + _map[r][nr]
//                                                                  + "\n";
                                                    write(_fileNameReport, outl);
                                                }

                                                int[] im = new int[8];

                                                im[0] = i;
                                                im[1] = j;
                                                im[2] = k;
                                                im[3] = r;
                                                im[4] = ni;
                                                im[5] = nj;
                                                im[6] = nk;
                                                im[7] = nr;
                                                _missing.add(im);


                                            }

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (Main.display_progress)
            {
                Main.increment_progress(2);
                print_progress();
            }
        }

        _n_tot_tway_cov = n_tot_tway_cov;
        _nComs = nComs;
        _tot_varvalconfig = tot_varvalconfigs4;
        _varvalStatN = varvalStats4;
        initialized = true;
    }

    private void FiveWay()
    {
        long n_tot_tway_cov = 0; // drk121109
        int i, j, k, r, x, ni, nj, nk, nr, nx, m, ti, tj, tk, tr;
        long[] varvalStats5 = new long[NBINS + 1]; // used to be int idm
        long n_varvalconfigs5;
        long nComs = 0; // number of combinations = C(ncols, t) //drk121109
        long tot_varvalconfigs5 = 0; // drk121109

        out_test = new int[_NGenTests][];
        Nout_test = 0;

        for (i = 0; i < NBINS + 1; i++)
        {
            varvalStats5[i] = 0;
        }

        CSolver validcomb = new CSolver();
        validcomb.SetConstraints(_constraints);
        validcomb.SetParameter(_parameters);

        _aInvalidComb = new ArrayList<>();
        _aInvalidNotIn = new ArrayList<>();

        for (i = _start; i < _end; i++)
        {
            for (j = i + 1; j < _ncols - 3; j++)
            {
                for (k = j + 1; k < _ncols - 2; k++)
                {
                    for (r = k + 1; r < _ncols - 1; r++)
                    {
                        for (x = r + 1; x < _ncols; x++)
                        {
                            nComs++;
                            int[][][][][] comcount = new int[_nvals[i]][][][][]; // allow
                            // row 0 for # values per parm
                            for (ti = 0; ti < _nvals[i]; ti++)
                            {
                                comcount[ti] = new int[_nvals[j]][][][];
                                for (tj = 0; tj < _nvals[j]; tj++)
                                {
                                    comcount[ti][tj] = new int[_nvals[k]][][];
                                    for (tk = 0; tk < _nvals[k]; tk++)
                                    {
                                        comcount[ti][tj][tk] = new int[_nvals[r]][];

                                        for (tr = 0; tr < _nvals[r]; tr++)
                                        {
                                            comcount[ti][tj][tk][tr] = new int[_nvals[x]];
                                        }
                                    }
                                }
                            }
                            String temp_key = String.format("%s(%d,%d,%d,%d,%d)", _tway, i, j, k, r, x);

                            // forall t-way combinations of input variable
                            // values:

                            // comcount i,j == 0
                            // for the combination designated by i,j increment
                            // counts
                            for (m = 0; m < _nrows; m++)
                            {
                                // mark if the var-val config is covered by the tests
                                // comcount[_test[m][i]][_test[m][j]][_test[m][k]][_test[m][r]][_test[m][x]] += 1;
                                // coumcount i,j == 1 iff some tests contains tuple i,j
                                int v1 = _test[m][i];
                                int v2 = _test[m][j];
                                int v3 = _test[m][k];
                                int v4 = _test[m][r];
                                int v5 = _test[m][x];
                                String[][] pars = new String[5][];
                                pars[0] = new String[2];
                                pars[1] = new String[2];
                                pars[2] = new String[2];
                                pars[3] = new String[2];
                                pars[4] = new String[2];

                                if (i >= _parameters.size() ||
                                    j >= _parameters.size() ||
                                    k >= _parameters.size() ||
                                    r >= _parameters.size() ||
                                    x >= _parameters.size())
                                {
                                    continue;
                                }
                                pars[0][0] = _parameters.get(i).getName();
                                pars[1][0] = _parameters.get(j).getName();
                                pars[2][0] = _parameters.get(k).getName();
                                pars[3][0] = _parameters.get(r).getName();
                                pars[4][0] = _parameters.get(x).getName();


                                if (v1 >= _parameters.get(i).getValues().size() ||
                                    v2 >= _parameters.get(j).getValues().size() ||
                                    v3 >= _parameters.get(k).getValues().size() ||
                                    v4 >= _parameters.get(r).getValues().size() ||
                                    v5 >= _parameters.get(x).getValues().size())
                                {
                                    continue;
                                }
                                pars[0][1] = _parameters.get(i).getValues().get(v1);
                                pars[1][1] = _parameters.get(j).getValues().get(v2);
                                pars[2][1] = _parameters.get(k).getValues().get(v3);
                                pars[3][1] = _parameters.get(r).getValues().get(v4);
                                pars[4][1] = _parameters.get(x).getValues().get(v5);
                                if (v1 < comcount.length)
                                {
                                    if (v2 < comcount[v1].length)
                                    {
                                        if (v3 < comcount[v1][v2].length)
                                        {
                                            if (v4 < comcount[v1][v2][v3].length)
                                            {
                                                if (v5 < comcount[v1][v2][v3][v4].length)
                                                {
                                                    if (_constraints.size() > 0)
                                                    {
                                                        if (validcomb.EvaluateCombination(pars))
                                                        {
                                                            comcount[v1][v2][v3][v4][v5] = 1;
                                                        } else
                                                        {
                                                            comcount[v1][v2][v3][v4][v5] = -1;
                                                        }
                                                    } else
                                                    {
                                                        comcount[v1][v2][v3][v4][v5] = 1;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            int varval_cnt = 0;
                            int invalidComb = 0;
                            int invalidcombNotCovered = 0;
                            // count how many value configs are contained in a
                            // test
                            for (ni = 0; ni < _nvals[i]; ni++)
                            {
                                for (nj = 0; nj < _nvals[j]; nj++)
                                {
                                    for (nk = 0; nk < _nvals[k]; nk++)
                                    {
                                        for (nr = 0; nr < _nvals[r]; nr++)
                                        {
                                            for (nx = 0; nx < _nvals[x]; nx++)
                                            {
                                                if (comcount[ni][nj][nk][nr][nx] == 1)
                                                {
                                                    varval_cnt++;
                                                    n_tot_tway_cov++;
                                                } else
                                                {
                                                    String[][] pars = new String[5][];
                                                    pars[0] = new String[2];
                                                    pars[1] = new String[2];
                                                    pars[2] = new String[2];
                                                    pars[3] = new String[2];
                                                    pars[4] = new String[2];

                                                    if (i >= _parameters.size() ||
                                                        j >= _parameters.size() ||
                                                        k >= _parameters.size() ||
                                                        r >= _parameters.size() ||
                                                        x >= _parameters.size())
                                                    {
                                                        continue;
                                                    }
                                                    pars[0][0] = _parameters.get(i).getName();
                                                    pars[1][0] = _parameters.get(j).getName();
                                                    pars[2][0] = _parameters.get(k).getName();
                                                    pars[3][0] = _parameters.get(r).getName();
                                                    pars[4][0] = _parameters.get(x).getName();

                                                    if (ni >= _parameters.get(i).getValues().size() ||
                                                        nj >= _parameters.get(j).getValues().size() ||
                                                        nk >= _parameters.get(k).getValues().size() ||
                                                        nr >= _parameters.get(r).getValues().size() ||
                                                        nx >= _parameters.get(x).getValues().size())
                                                    {
                                                        continue;
                                                    }
                                                    pars[0][1] = _parameters.get(i).getValues().get(ni);
                                                    pars[1][1] = _parameters.get(j).getValues().get(nj);
                                                    pars[2][1] = _parameters.get(k).getValues().get(nk);
                                                    pars[3][1] = _parameters.get(r).getValues().get(nr);
                                                    pars[4][1] = _parameters.get(x).getValues().get(nx);

                                                    if (comcount[ni][nj][nk][nr][nx] == -1)
                                                    {
                                                        invalidComb += 1;
                                                        _aInvalidComb.add(pars);
                                                    }

                                                    if (comcount[ni][nj][nk][nr][nx] == 0
                                                        && _constraints.size() > 0
                                                        && _nrows > 0)
                                                    {
                                                        if (!validcomb.EvaluateCombination(pars))
                                                        {
                                                            invalidcombNotCovered += 1;
                                                            _aInvalidNotIn.add(pars);
                                                            comcount[ni][nj][nk][nr][nx] = -3;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            comcount_array5.put(temp_key, comcount);
                            n_varvalconfigs5 = _nvals[i] * _nvals[j] * _nvals[k] * _nvals[r] * _nvals[x];
                            n_varvalconfigs5 -= (invalidComb + invalidcombNotCovered);
                            tot_varvalconfigs5 += n_varvalconfigs5;

                            double varval_cov = (double) varval_cnt / (double) n_varvalconfigs5;
                            // varval_cov bins give the number of var/val
                            // configurations covered
                            // at the levels: 0, [5,10), [10,15) etc. (assume 20
                            // bins)
                            for (int b = 0; b <= NBINS; b++)
                            {
                                if (varval_cov >= (double) b / (double) NBINS)
                                {
                                    varvalStats5[b]++;
                                }
                            }

                            // *********** For missing combinations
                            if (_GenTests && varval_cov < _minCov)
                            {
                                for (ni = 0; ni < _nvals[i]; ni++)
                                {
                                    for (nj = 0; nj < _nvals[j]; nj++)
                                    {
                                        for (nk = 0; nk < _nvals[k]; nk++)
                                        {
                                            for (nr = 0; nr < _nvals[r]; nr++)
                                            {
                                                for (nx = 0; nx < _nvals[x]; nx++)
                                                {
                                                    if (comcount[ni][nj][nk][nr][nx] == 0)
                                                    {
                                                        // not covered; write out combination to cover
                                                        if (_rptMissingCom)
                                                        {
                                                            String outl = String.format(
                                                                                               "%d,%d,%d,%d,%d = %d,%d,%d,%d,%d || %s,%s,%s,%s,%s\n",
                                                                                               i,
                                                                                               j,
                                                                                               k,
                                                                                               r,
                                                                                               x,
                                                                                               ni,
                                                                                               nj,
                                                                                               nk,
                                                                                               nr,
                                                                                               nx,
                                                                                               _map[i][ni],
                                                                                               _map[i][ni],
                                                                                               _map[k][nk],
                                                                                               _map[r][nr],
                                                                                               _map[x][nx]
                                                                                       );
                                                            write(_fileNameReport, outl);

                                                        }

                                                        int[] im = new int[10];

                                                        im[0] = i;
                                                        im[1] = j;
                                                        im[2] = k;
                                                        im[3] = r;
                                                        im[4] = x;
                                                        im[5] = ni;
                                                        im[6] = nj;
                                                        im[7] = nk;
                                                        im[8] = nr;
                                                        im[9] = nx;
                                                        _missing.add(im);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (Main.display_progress)
            {
                Main.increment_progress(3);
                print_progress();
            }
        }

        _n_tot_tway_cov = n_tot_tway_cov;
        _nComs = nComs;
        _tot_varvalconfig = tot_varvalconfigs5;
        _varvalStatN = varvalStats5;
        initialized = true;
    }

    private void SixWay()
    {
        long n_tot_tway_cov = 0; // drk121109
        int i, j, k, r, x, z, ni, nj, nk, nr, nx, nz, m, ti, tj, tk, tr, tx;
        long[] varvalStats6 = new long[NBINS + 1];
        long n_varvalconfigs6;
        long nComs = 0; // number of combinations = C(ncols, t) //drk121109
        long tot_varvalconfigs6 = 0; // drk121109

        out_test = new int[_NGenTests][];
        Nout_test = 0;

        for (i = 0; i < NBINS + 1; i++)
        {
            varvalStats6[i] = 0;
        }

        CSolver validcomb = new CSolver();
        validcomb.SetConstraints(_constraints);
        validcomb.SetParameter(_parameters);

        _aInvalidComb = new ArrayList<>();
        _aInvalidNotIn = new ArrayList<>();

        for (i = _start; i < _end; i++)
        {
            for (j = i + 1; j < _ncols - 4; j++)
            {
                for (k = j + 1; k < _ncols - 3; k++)
                {
                    for (r = k + 1; r < _ncols - 2; r++)
                    {
                        for (x = r + 1; x < _ncols - 1; x++)
                        {
                            for (z = x + 1; z < _ncols; z++)
                            {
                                nComs++;
                                int[][][][][][] comcount = new int[_nvals[i]][][][][][]; // allow
                                // row  0 for # alues per parm
                                for (ti = 0; ti < _nvals[i]; ti++)
                                {
                                    comcount[ti] = new int[_nvals[j]][][][][];
                                    for (tj = 0; tj < _nvals[j]; tj++)
                                    {
                                        comcount[ti][tj] = new int[_nvals[k]][][][];
                                        for (tk = 0; tk < _nvals[k]; tk++)
                                        {
                                            comcount[ti][tj][tk] = new int[_nvals[r]][][];

                                            for (tr = 0; tr < _nvals[r]; tr++)
                                            {
                                                comcount[ti][tj][tk][tr] = new int[_nvals[x]][];
                                                for (tx = 0; tx < _nvals[x]; tx++)
                                                {
                                                    comcount[ti][tj][tk][tr][tx] = new int[_nvals[z]];
                                                }
                                            }
                                        }
                                    }
                                }
                                String temp_key = String.format("%s(%d,%d,%d,%d,%d,%d)", _tway, i, j, k, r, x, z);

                                // forall t-way combinations of input variable
                                // values:
                                //

                                // comcount i,j == 0
                                // for the combination designated by i,j
                                // increment counts
                                for (m = 0; m < _nrows; m++)
                                {
                                    // mark if the var-val config is covered by the tests
                                    // comcount[_test[m][i]][_test[m][j]][_test[m][k]][_test[m][r]][_test[m][x]][_test[m][z]] += 1;
                                    // coumcount i,j == 1 iff some  tests contains tuple i,j
                                    int v1 = _test[m][i];
                                    int v2 = _test[m][j];
                                    int v3 = _test[m][k];
                                    int v4 = _test[m][r];
                                    int v5 = _test[m][x];
                                    int v6 = _test[m][z];
                                    String[][] pars = new String[6][];
                                    pars[0] = new String[2];
                                    pars[1] = new String[2];
                                    pars[2] = new String[2];
                                    pars[3] = new String[2];
                                    pars[4] = new String[2];
                                    pars[5] = new String[2];

                                    if (i >= _parameters.size() ||
                                        j >= _parameters.size() ||
                                        k >= _parameters.size() ||
                                        r >= _parameters.size() ||
                                        x >= _parameters.size() ||
                                        z >= _parameters.size())
                                    {
                                        continue;
                                    }
                                    pars[0][0] = _parameters.get(i).getName();
                                    pars[1][0] = _parameters.get(j).getName();
                                    pars[2][0] = _parameters.get(k).getName();
                                    pars[3][0] = _parameters.get(r).getName();
                                    pars[4][0] = _parameters.get(x).getName();
                                    pars[5][0] = _parameters.get(z).getName();


                                    if (v1 >= _parameters.get(i).getValues().size() ||
                                        v2 >= _parameters.get(j).getValues().size() ||
                                        v3 >= _parameters.get(k).getValues().size() ||
                                        v4 >= _parameters.get(r).getValues().size() ||
                                        v5 >= _parameters.get(x).getValues().size() ||
                                        v6 >= _parameters.get(z).getValues().size())
                                    {
                                        continue;
                                    }
                                    pars[0][1] = _parameters.get(i).getValues().get(v1);
                                    pars[1][1] = _parameters.get(j).getValues().get(v2);
                                    pars[2][1] = _parameters.get(k).getValues().get(v3);
                                    pars[3][1] = _parameters.get(r).getValues().get(v4);
                                    pars[4][1] = _parameters.get(x).getValues().get(v5);
                                    pars[5][1] = _parameters.get(z).getValues().get(v6);
                                    if (v1 < comcount.length)
                                    {
                                        if (v2 < comcount[v1].length)
                                        {
                                            if (v3 < comcount[v1][v2].length)
                                            {
                                                if (v4 < comcount[v1][v2][v3].length)
                                                {
                                                    if (v5 < comcount[v1][v2][v3][v4].length)
                                                    {
                                                        if (v6 < comcount[v1][v2][v3][v4][v5].length)
                                                        {
                                                            if (_constraints.size() > 0)
                                                            {
                                                                if (validcomb.EvaluateCombination(pars))
                                                                {
                                                                    comcount[v1][v2][v3][v4][v5][v6] = 1;
                                                                } else
                                                                {
                                                                    comcount[v1][v2][v3][v4][v5][v6] = -1;
                                                                }

                                                            } else
                                                            {
                                                                comcount[v1][v2][v3][v4][v5][v6] = 1;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                int varval_cnt = 0;
                                int invalidComb = 0;
                                int invalidcombNotCovered = 0;
                                // count how many value configs are contained in
                                // a test
                                for (ni = 0; ni < _nvals[i]; ni++)
                                {
                                    for (nj = 0; nj < _nvals[j]; nj++)
                                    {
                                        for (nk = 0; nk < _nvals[k]; nk++)
                                        {
                                            for (nr = 0; nr < _nvals[r]; nr++)
                                            {
                                                for (nx = 0; nx < _nvals[x]; nx++)
                                                {
                                                    for (nz = 0; nz < _nvals[z]; nz++)
                                                    {
                                                        if (comcount[ni][nj][nk][nr][nx][nz] == 1)
                                                        {
                                                            varval_cnt++;
                                                            n_tot_tway_cov++;
                                                        } else
                                                        {
                                                            String[][] pars = new String[6][];
                                                            pars[0] = new String[2];
                                                            pars[1] = new String[2];
                                                            pars[2] = new String[2];
                                                            pars[3] = new String[2];
                                                            pars[4] = new String[2];
                                                            pars[5] = new String[2];

                                                            if (i >= _parameters.size() ||
                                                                j >= _parameters.size() ||
                                                                k >= _parameters.size() ||
                                                                r >= _parameters.size() ||
                                                                x >= _parameters.size() ||
                                                                z >= _parameters.size())
                                                            {
                                                                continue;
                                                            }
                                                            pars[0][0] = _parameters.get(i).getName();
                                                            pars[1][0] = _parameters.get(j).getName();
                                                            pars[2][0] = _parameters.get(k).getName();
                                                            pars[3][0] = _parameters.get(r).getName();
                                                            pars[4][0] = _parameters.get(x).getName();
                                                            pars[5][0] = _parameters.get(z).getName();

                                                            if (ni >= _parameters.get(i).getValues().size() ||
                                                                nj >= _parameters.get(j).getValues().size() ||
                                                                nk >= _parameters.get(k).getValues().size() ||
                                                                nr >= _parameters.get(r).getValues().size() ||
                                                                nx >= _parameters.get(x).getValues().size() ||
                                                                nz >= _parameters.get(z).getValues().size())
                                                            {
                                                                continue;
                                                            }
                                                            pars[0][1] = _parameters.get(i).getValues().get(ni);
                                                            pars[1][1] = _parameters.get(j).getValues().get(nj);
                                                            pars[2][1] = _parameters.get(k).getValues().get(nk);
                                                            pars[3][1] = _parameters.get(r).getValues().get(nr);
                                                            pars[4][1] = _parameters.get(x).getValues().get(nx);
                                                            pars[5][1] = _parameters.get(z).getValues().get(nz);

                                                            if (comcount[ni][nj][nk][nr][nx][nz] == -1)
                                                            {
                                                                invalidComb += 1;
                                                                _aInvalidComb.add(pars);
                                                            }

                                                            if (comcount[ni][nj][nk][nr][nx][nz] == 0
                                                                && _constraints.size() > 0 && _nrows > 0)
                                                            {
                                                                if (!validcomb.EvaluateCombination(pars))
                                                                {
                                                                    invalidcombNotCovered += 1;
                                                                    _aInvalidNotIn.add(pars);
                                                                    comcount[ni][nj][nk][nr][nx][nz] = -3;
                                                                }

                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                comcount_array6.put(temp_key, comcount);
                                n_varvalconfigs6 = _nvals[i] * _nvals[j] * _nvals[k] * _nvals[r] * _nvals[x]
                                                   * _nvals[z];
                                n_varvalconfigs6 -= (invalidComb + invalidcombNotCovered);
                                tot_varvalconfigs6 += n_varvalconfigs6;

                                double varval_cov = (double) varval_cnt / (double) n_varvalconfigs6;
                                // varval_cov bins give the number of var/val
                                // configurations covered
                                // at the levels: 0, [5,10), [10,15) etc.
                                // (assume 20 bins)
                                for (int b = 0; b <= NBINS; b++)
                                {
                                    if (varval_cov >= (double) b / (double) NBINS)
                                    {
                                        varvalStats6[b]++;
                                    }
                                }

                                // *********** For missing combinations
                                if (_GenTests && varval_cov < _minCov)
                                {
                                    for (ni = 0; ni < _nvals[i]; ni++)
                                    {
                                        for (nj = 0; nj < _nvals[j]; nj++)
                                        {
                                            for (nk = 0; nk < _nvals[k]; nk++)
                                            {
                                                for (nr = 0; nr < _nvals[r]; nr++)
                                                {
                                                    for (nx = 0; nx < _nvals[x]; nx++)
                                                    {
                                                        for (nz = 0; nz < _nvals[z]; nz++)
                                                        {
                                                            if (comcount[ni][nj][nk][nr][nx][nz] == 0)
                                                            {
                                                                // not covered; write out combination to cover
                                                                if (_rptMissingCom)
                                                                {
                                                                    String outl = String.format(
                                                                                                       "%d,%d,%d,%d,%d,%d = %d,%d,%d,%d,%d,%d || %s,%s,%s,%s,%s,%s\n",
                                                                                                       i,
                                                                                                       j,
                                                                                                       k,
                                                                                                       r,
                                                                                                       x,
                                                                                                       z,
                                                                                                       ni,
                                                                                                       nj,
                                                                                                       nk,
                                                                                                       nr,
                                                                                                       nx,
                                                                                                       nz,
                                                                                                       _map[i][ni],
                                                                                                       _map[i][ni],
                                                                                                       _map[k][nk],
                                                                                                       _map[r][nr],
                                                                                                       _map[x][nx],
                                                                                                       _map[z][nz]
                                                                                               );
                                                                    write(_fileNameReport, outl);
                                                                }
                                                                int[] im = new int[12];

                                                                im[0] = i;
                                                                im[1] = j;
                                                                im[2] = k;
                                                                im[3] = r;
                                                                im[4] = x;
                                                                im[5] = z;
                                                                im[6] = ni;
                                                                im[7] = nj;
                                                                im[8] = nk;
                                                                im[9] = nr;
                                                                im[10] = nx;
                                                                im[11] = nz;
                                                                _missing.add(im);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (Main.display_progress)
            {
                Main.increment_progress(4);
                print_progress();
            }

        }

        _n_tot_tway_cov = n_tot_tway_cov;
        _nComs = nComs;
        _tot_varvalconfig = tot_varvalconfigs6;
        _varvalStatN = varvalStats6;
        initialized = true;
    }

    @Override
    protected Object compute()
    {
        if (!_parallel)
        {
            out_test = new int[_NGenTests][];
            Nout_test = 0;
            GetTway();
            if (!wait)
            {
                switch (_tway)
                {
                    case "2way":
                        two_way_missing(_missing);
                        break;
                    case "3way":
                        three_way_missing(_missing);
                        break;
                    case "4way":
                        four_way_missing(_missing);
                        break;
                    case "5way":
                        five_way_missing(_missing);
                        break;
                    case "6way":
                        six_way_missing(_missing);
                        break;
                }
                if (_GenTests)
                {
                    GenerateMissingFile(out_test, Nout_test);
                }
            }
            return 1;
        }


        int cores = Runtime.getRuntime().availableProcessors();

        Tway[] list = new Tway[cores];

        int piece = (_end) / cores;

        for (int i = 0; i < cores; i++)
        {
            int start = i * piece;
            int end = (i == cores - 1) ? _end : start + piece;


            list[i] = new Tway(_tway, start, end, _test, _nvals, _nrows, _ncols, _parameters, _constraints, _map);

            list[i].set_bnd(_bnd);
            list[i].set_Rng(_rng);
            list[i].set_grp(_grp);
            list[i].set_wait(true);

            if (_GenTests)
            {
                list[i].set_appendTests(_appendTests);
                list[i].set_GenTests(_GenTests);
                list[i].set_FileNameMissing(_fileNameMissing);
                list[i].set_rptMissingCom(_rptMissingCom);
                list[i].set_minCov(_minCov);
                list[i].set_NGenTests(_MaxGenTests);
                list[i].set_FileNameReport(_fileNameReport);
                list[i].set_map(_map);

            }
            list[i].fork();
        }

        _varvalStatN = new long[NBINS + 1];
        hm_colors2 = new int[_ncols][];
        for (int i = 0; i < _ncols; i++)
        {
            hm_colors2[i] = new int[_ncols];
        }
        _aInvalidComb = new ArrayList<>();
        _aInvalidNotIn = new ArrayList<>();

        out_test = new int[_NGenTests][];
        Nout_test = 0;

        for (int i = 0; i < cores; i++)
        {
            list[i].join();
            _n_tot_tway_cov = _n_tot_tway_cov + list[i]._n_tot_tway_cov;
            _nComs = _nComs + list[i]._nComs;
            _tot_varvalconfig = _tot_varvalconfig + list[i]._tot_varvalconfig;

            for (int comb = 0; comb < list[i]._aInvalidComb.size(); comb++)
            {
                _aInvalidComb.add(list[i]._aInvalidComb.get(comb));
            }

            for (int comb = 0; comb < list[i]._aInvalidNotIn.size(); comb++)
            {
                _aInvalidNotIn.add(list[i]._aInvalidNotIn.get(comb));
            }

            for (int x = 0; x < list[i]._varvalStatN.length; x++)
            {
                _varvalStatN[x] = _varvalStatN[x] + list[i]._varvalStatN[x];
            }

            if (Objects.equals(_tway, "2way"))
            {
                for (int ii = 0; ii < _ncols; ii++)
                {
                    for (int jj = 0; jj < _ncols; jj++)
                    {
                        hm_colors2[ii][jj] = hm_colors2[ii][jj] + list[i].hm_colors2[ii][jj];
                    }
                }
                two_way_missing(list[i]._missing);
            }

            if (Objects.equals(_tway, "3way"))
            {
                three_way_missing(list[i]._missing);
            }
            if (Objects.equals(_tway, "4way"))
            {
                four_way_missing(list[i]._missing);
            }
            if (Objects.equals(_tway, "5way"))
            {
                five_way_missing(list[i]._missing);
            }
            if (Objects.equals(_tway, "6way"))
            {
                six_way_missing(list[i]._missing);
            }

        }
        if (_GenTests)
        {
            GenerateMissingFile(out_test, Nout_test);
        }
        return 1;
    }

    private void two_way_missing(List<int[]> l)
    {
        for (int ii = 0; ii < _ncols; ii++)
        {
            for (int jj = 0; jj < _ncols; jj++)
            {
                for (int[] mis : l)
                {
                    if (mis[0] == ii && mis[1] == jj)
                    {
                        Boolean comout = false;
                        int r;
                        for (r = 0; r < Nout_test && !comout; r++)
                        {
                            if (out_test[r][ii] < 0 && out_test[r][jj] < 0)
                            {
                                out_test[r][ii] = mis[2];
                                out_test[r][jj] = mis[3];
                                comout = true;
                            }
                            if (out_test[r][ii] == mis[2] && out_test[r][jj] < 0)
                            {
                                out_test[r][jj] = mis[3];
                                comout = true;
                            }
                            if (out_test[r][ii] < 0 && out_test[r][jj] == mis[3])
                            {
                                out_test[r][ii] = mis[2];
                                comout = true;
                            }
                            if (out_test[r][ii] == mis[2] && out_test[r][jj] == mis[3])
                            {
                                comout = true;
                            }
                        }
                        if (!comout && Nout_test < _MaxGenTests)
                        {
                            // com was not output to test; add new test
                            out_test[r] = new int[_ncols];
                            for (int x = 0; x < _ncols; x++)
                            {
                                out_test[r][x] = -1; // init empty test
                            }
                            out_test[r][ii] = mis[2];
                            out_test[r][jj] = mis[3];
                            Nout_test++;
                        }
                    }
                }
            }
        }
    }

    private void three_way_missing(List<int[]> l)
    {
        for (int ii = 0; ii < _ncols; ii++)
        {
            for (int jj = 0; jj < _ncols; jj++)
            {
                for (int kk = 0; kk < _ncols; kk++)
                {
                    for (int[] mis : l)
                    {
                        if (mis[0] == ii && mis[1] == jj && mis[2] == kk)
                        {
                            // value i may be covered but not value j
                            Boolean comout = false;
                            int r;
                            for (r = 0; r < Nout_test && !comout; r++)
                            {
                                if (out_test[r][ii] < 0 && out_test[r][jj] < 0 && out_test[r][kk] < 0)
                                {
                                    out_test[r][ii] = mis[3];
                                    out_test[r][jj] = mis[4];
                                    out_test[r][kk] = mis[5];
                                    comout = true;
                                }
                                if (out_test[r][ii] == mis[3] && out_test[r][jj] < 0 && out_test[r][kk] < 0)
                                {
                                    out_test[r][jj] = mis[4];
                                    out_test[r][kk] = mis[5];
                                    comout = true;
                                }
                                if (out_test[r][ii] < 0 && out_test[r][jj] == mis[4] && out_test[r][kk] < 0)
                                {
                                    out_test[r][ii] = mis[3];
                                    out_test[r][kk] = mis[5];
                                    comout = true;
                                }
                                if (out_test[r][ii] < 0 && out_test[r][jj] < 0 && out_test[r][kk] == mis[5])
                                {
                                    out_test[r][ii] = mis[3];
                                    out_test[r][jj] = mis[4];
                                    comout = true;
                                }
                                if (out_test[r][ii] == mis[3] && out_test[r][jj] == mis[4]
                                    && out_test[r][kk] == mis[5])
                                {
                                    comout = true;
                                }
                            }
                            if (!comout && Nout_test < _MaxGenTests)
                            {
                                // com was not output to _ncols
                                out_test[r] = new int[_ncols];
                                for (int x = 0; x < _ncols; x++)
                                {
                                    out_test[r][x] = -1;
                                }
                                out_test[r][ii] = mis[3];
                                out_test[r][jj] = mis[4];
                                out_test[r][kk] = mis[5];
                                Nout_test++;
                            }
                        }
                    }
                }
            }
        }
    }

    private void four_way_missing(List<int[]> l)
    {
        for (int ii = 0; ii < _ncols; ii++)
        {
            for (int jj = 0; jj < _ncols; jj++)
            {
                for (int kk = 0; kk < _ncols; kk++)
                {

                    for (int rr = 0; rr < _ncols; rr++)
                    {
                        for (int[] mis : l)
                        {
                            if (mis[0] == ii && mis[1] == jj && mis[2] == kk && mis[3] == rr)
                            {

                                Boolean comout = false;
                                int w;
                                for (w = 0; w < Nout_test && !comout; w++)
                                {
                                    if (out_test[w][ii] < 0 && out_test[w][jj] < 0 && out_test[w][kk] < 0
                                        && out_test[w][rr] < 0)
                                    {
                                        out_test[w][ii] = mis[4];
                                        out_test[w][jj] = mis[5];
                                        out_test[w][kk] = mis[6];
                                        out_test[w][rr] = mis[7];
                                        comout = true;
                                    }
                                    if (out_test[w][ii] == mis[4] && out_test[w][jj] < 0 && out_test[w][kk] < 0
                                        && out_test[w][rr] < 0)
                                    {
                                        out_test[w][jj] = mis[5];
                                        out_test[w][kk] = mis[6];
                                        out_test[w][rr] = mis[7];
                                        comout = true;
                                    }
                                    if (out_test[w][ii] < 0 && out_test[w][jj] == mis[5] && out_test[w][kk] < 0
                                        && out_test[w][rr] < 0)
                                    {
                                        out_test[w][ii] = mis[4];
                                        out_test[w][kk] = mis[6];
                                        out_test[w][rr] = mis[7];
                                        comout = true;
                                    }
                                    if (out_test[w][ii] < 0 && out_test[w][jj] < 0 && out_test[w][kk] == mis[6]
                                        && out_test[w][rr] < 0)
                                    {
                                        out_test[w][ii] = mis[4];
                                        out_test[w][jj] = mis[5];
                                        out_test[w][rr] = mis[7];
                                        comout = true;
                                    }

                                    if (out_test[w][ii] < 0 && out_test[w][jj] < 0 && out_test[w][kk] < 0
                                        && out_test[w][rr] == mis[7])
                                    {
                                        out_test[w][ii] = mis[4];
                                        out_test[w][jj] = mis[5];
                                        out_test[w][kk] = mis[6];
                                        comout = true;
                                    }

                                    if (out_test[w][ii] == mis[4] && out_test[w][jj] == mis[5]
                                        && out_test[w][kk] == mis[6] && out_test[w][rr] == mis[7])
                                    {
                                        comout = true;
                                    }
                                }
                                if (!comout && Nout_test < _MaxGenTests)
                                {
                                    // com was not output to
                                    out_test[w] = new int[_ncols];
                                    for (int x = 0; x < _ncols; x++)
                                    {
                                        out_test[w][x] = -1;
                                    }
                                    out_test[w][ii] = mis[4];
                                    out_test[w][jj] = mis[5];
                                    out_test[w][kk] = mis[6];
                                    out_test[w][rr] = mis[7];
                                    Nout_test++;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void five_way_missing(List<int[]> l)
    {
        for (int ii = 0; ii < _ncols; ii++)
        {
            for (int jj = 0; jj < _ncols; jj++)
            {
                for (int kk = 0; kk < _ncols; kk++)
                {
                    for (int rr = 0; rr < _ncols; rr++)
                    {
                        for (int xx = 0; xx < _ncols; xx++)
                        {
                            for (int[] mis : l)
                            {
                                if (mis[0] == ii && mis[1] == jj && mis[2] == kk && mis[3] == rr && mis[4] == xx)
                                {
                                    Boolean comout = false;
                                    int w;
                                    for (w = 0; w < Nout_test && !comout; w++)
                                    {
                                        if (out_test[w][ii] < 0 && out_test[w][jj] < 0 && out_test[w][kk] < 0
                                            && out_test[w][rr] < 0 && out_test[w][xx] < 0)
                                        {
                                            out_test[w][ii] = mis[5];
                                            out_test[w][jj] = mis[6];
                                            out_test[w][kk] = mis[7];
                                            out_test[w][rr] = mis[8];
                                            out_test[w][xx] = mis[9];
                                            comout = true;
                                        }
                                        if (out_test[w][ii] == mis[5] && out_test[w][jj] < 0 && out_test[w][kk] < 0
                                            && out_test[w][rr] < 0 && out_test[w][xx] < 0)
                                        {
                                            out_test[w][jj] = mis[6];
                                            out_test[w][kk] = mis[7];
                                            out_test[w][rr] = mis[8];
                                            out_test[w][xx] = mis[9];
                                            comout = true;
                                        }
                                        if (out_test[w][ii] < 0 && out_test[w][jj] == mis[6] && out_test[w][kk] < 0
                                            && out_test[w][rr] < 0 && out_test[w][xx] < 0)
                                        {
                                            out_test[w][ii] = mis[5];
                                            out_test[w][kk] = mis[7];
                                            out_test[w][rr] = mis[8];
                                            out_test[w][xx] = mis[9];
                                            comout = true;
                                        }
                                        if (out_test[w][ii] < 0 && out_test[w][jj] < 0 && out_test[w][kk] == mis[7]
                                            && out_test[w][rr] < 0 && out_test[w][xx] < 0)
                                        {
                                            out_test[w][ii] = mis[5];
                                            out_test[w][jj] = mis[6];
                                            out_test[w][rr] = mis[8];
                                            out_test[w][xx] = mis[9];
                                            comout = true;
                                        }

                                        if (out_test[w][ii] < 0 && out_test[w][jj] < 0 && out_test[w][kk] < 0
                                            && out_test[w][rr] == mis[8] && out_test[w][xx] < 0)
                                        {
                                            out_test[w][ii] = mis[5];
                                            out_test[w][jj] = mis[6];
                                            out_test[w][kk] = mis[7];
                                            out_test[w][xx] = mis[9];
                                            comout = true;
                                        }

                                        if (out_test[w][ii] < 0 && out_test[w][jj] < 0 && out_test[w][kk] < 0
                                            && out_test[w][rr] < 0 && out_test[w][xx] == mis[9])
                                        {
                                            out_test[w][ii] = mis[5];
                                            out_test[w][jj] = mis[6];
                                            out_test[w][kk] = mis[7];
                                            out_test[w][rr] = mis[8];
                                            comout = true;
                                        }

                                        if (out_test[w][ii] == mis[5] && out_test[w][jj] == mis[6]
                                            && out_test[w][kk] == mis[7] && out_test[w][rr] == mis[8]
                                            && out_test[w][xx] == mis[9])
                                        {
                                            comout = true;
                                        }
                                    }
                                    if (!comout && Nout_test < _MaxGenTests)
                                    {
                                        // com was not output to
                                        out_test[w] = new int[_ncols];
                                        for (int x = 0; x < _ncols; x++)
                                        {
                                            out_test[w][x] = -1;
                                        }
                                        out_test[w][ii] = mis[5];
                                        out_test[w][jj] = mis[6];
                                        out_test[w][kk] = mis[7];
                                        out_test[w][rr] = mis[8];
                                        out_test[w][xx] = mis[9];
                                        Nout_test++;
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void six_way_missing(List<int[]> l)
    {
        for (int ii = 0; ii < _ncols; ii++)
        {
            for (int jj = 0; jj < _ncols; jj++)
            {
                for (int kk = 0; kk < _ncols; kk++)
                {
                    for (int rr = 0; rr < _ncols; rr++)
                    {
                        for (int xx = 0; xx < _ncols; xx++)
                        {
                            for (int zz = 0; zz < _ncols; zz++)
                            {
                                for (int[] mis : l)
                                {
                                    if (mis[0] == ii && mis[1] == jj && mis[2] == kk && mis[3] == rr && mis[4] == xx
                                        && mis[5] == zz)
                                    {

                                        Boolean comout = false;
                                        int w;
                                        for (w = 0; w < Nout_test && !comout; w++)
                                        {
                                            if (out_test[w][ii] < 0 && out_test[w][jj] < 0 && out_test[w][kk] < 0
                                                && out_test[w][rr] < 0 && out_test[w][xx] < 0
                                                && out_test[w][zz] < 0)
                                            {
                                                out_test[w][ii] = mis[6];
                                                out_test[w][jj] = mis[7];
                                                out_test[w][kk] = mis[8];
                                                out_test[w][rr] = mis[9];
                                                out_test[w][xx] = mis[10];
                                                out_test[w][zz] = mis[11];
                                                comout = true;
                                            }
                                            if (out_test[w][ii] == mis[6] && out_test[w][jj] < 0 && out_test[w][kk] < 0
                                                && out_test[w][rr] < 0 && out_test[w][xx] < 0
                                                && out_test[w][zz] < 0)
                                            {
                                                out_test[w][jj] = mis[7];
                                                out_test[w][kk] = mis[8];
                                                out_test[w][rr] = mis[9];
                                                out_test[w][xx] = mis[10];
                                                out_test[w][zz] = mis[11];
                                                comout = true;
                                            }
                                            if (out_test[w][ii] < 0 && out_test[w][jj] == mis[7] && out_test[w][kk] < 0
                                                && out_test[w][rr] < 0 && out_test[w][xx] < 0
                                                && out_test[w][zz] < 0)
                                            {
                                                out_test[w][ii] = mis[6];
                                                out_test[w][kk] = mis[8];
                                                out_test[w][rr] = mis[9];
                                                out_test[w][xx] = mis[10];
                                                out_test[w][zz] = mis[11];
                                                comout = true;
                                            }
                                            if (out_test[w][ii] < 0 && out_test[w][jj] < 0 && out_test[w][kk] == mis[8]
                                                && out_test[w][rr] < 0 && out_test[w][xx] < 0
                                                && out_test[w][zz] < 0)
                                            {
                                                out_test[w][ii] = mis[6];
                                                out_test[w][jj] = mis[7];
                                                out_test[w][rr] = mis[9];
                                                out_test[w][xx] = mis[10];
                                                out_test[w][zz] = mis[11];
                                                comout = true;
                                            }

                                            if (out_test[w][ii] < 0 && out_test[w][jj] < 0 && out_test[w][kk] < 0
                                                && out_test[w][rr] == mis[9] && out_test[w][xx] < 0
                                                && out_test[w][zz] < 0)
                                            {
                                                out_test[w][ii] = mis[6];
                                                out_test[w][jj] = mis[7];
                                                out_test[w][kk] = mis[8];
                                                out_test[w][xx] = mis[10];
                                                out_test[w][zz] = mis[11];
                                                comout = true;
                                            }

                                            if (out_test[w][ii] < 0 && out_test[w][jj] < 0 && out_test[w][kk] < 0
                                                && out_test[w][rr] < 0 && out_test[w][xx] == mis[10]
                                                && out_test[w][zz] < 0)
                                            {
                                                out_test[w][ii] = mis[6];
                                                out_test[w][jj] = mis[7];
                                                out_test[w][kk] = mis[8];
                                                out_test[w][rr] = mis[9];
                                                out_test[w][zz] = mis[11];
                                                comout = true;
                                            }

                                            if (out_test[w][ii] < 0 && out_test[w][jj] < 0 && out_test[w][kk] < 0
                                                && out_test[w][rr] < 0 && out_test[w][xx] < 0
                                                && out_test[w][zz] == mis[11])
                                            {
                                                out_test[w][ii] = mis[6];
                                                out_test[w][jj] = mis[7];
                                                out_test[w][kk] = mis[8];
                                                out_test[w][rr] = mis[9];
                                                out_test[w][xx] = mis[10];
                                                comout = true;
                                            }

                                            if (out_test[w][ii] == mis[6] && out_test[w][jj] == mis[7]
                                                && out_test[w][kk] == mis[8] && out_test[w][rr] == mis[9]
                                                && out_test[w][xx] == mis[10] && out_test[w][zz] == mis[11])
                                            {
                                                comout = true;
                                            }
                                        }
                                        if (!comout && Nout_test < _MaxGenTests)
                                        {
                                            // com was not output to
                                            out_test[w] = new int[_ncols];
                                            for (int x = 0; x < _ncols; x++)
                                            {
                                                out_test[w][x] = -1;
                                            }
                                            out_test[w][ii] = mis[6];
                                            out_test[w][jj] = mis[7];
                                            out_test[w][kk] = mis[8];
                                            out_test[w][rr] = mis[9];
                                            out_test[w][xx] = mis[10];
                                            out_test[w][zz] = mis[11];
                                            Nout_test++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void GenerateMissingFile(int[][] out, int No_out)
    {
        try
        {

            if (Integer.parseInt(String.valueOf(_tway.charAt(0))) != Main.tway_max)
            {
                return;
            }
            CSolver validcomb = new CSolver();
            validcomb.SetConstraints(_constraints);
            validcomb.SetParameter(_parameters);

            //Add parameter names to file
            if (_parmName == 1)
            {
                String line = "";
                for (Parameter p : _parameters)
                {
                    line = line + p.getName() + ",";
                }
                while (line.endsWith(","))
                {
                    line = line.substring(0, line.length() - 1);
                }
                write(_fileNameMissing, line);
            }

            if (_appendTests)
            { // append new tests to original, so write out
                // original first

                //if (_constraints.size() > 0)
                //for (meConstraint c : _constraints)
                //write(_fileNameMissing, c.get_cons());

				/*
                    for (int jj = 0; jj < _ncols; jj++) {
						outl += _map[jj][_test[ii][jj]];
						if (jj < _ncols - 1)
							outl += ",";
					}
					*/
                for (int i = 0; i < Main.infile.length; i++)
                {
                    write(_fileNameMissing, Main.infile[i]);
                }


                //}
            }


            for (int ii = 0; ii < No_out; ii++)
            {
                String outl = "";
                for (int jj = 0; jj < _ncols; jj++)
                {
                    int ntmp = out[ii][jj];
                    String[][] parV;

                    if (ntmp > 0 && _constraints.size() > 0)
                    {
                        parV = new String[1][];
                        parV[0] = new String[2];
                        parV[0][0] = _parameters.get(jj).getName();
                        parV[0][1] = _parameters.get(jj).getValues().get(ntmp);

                        if (!validcomb.EvaluateCombination(parV))
                        {
                            ntmp = -1;
                        }
                    }
                    if (ntmp < 0)
                    {
                        // output value from input test file mapped by index
                        // if there are constraints
                        // the valid value will
                        // be search
                        if (_constraints.size() > 0)
                        {
                            do
                            {
                                ntmp++;
                                parV = new String[1][];
                                parV[0] = new String[2];
                                parV[0][0] = _parameters.get(jj).getName();
                                parV[0][1] = _parameters.get(jj).getValues().get(ntmp);
                            }
                            while (!validcomb.EvaluateCombination(parV));
                        } else
                        {
                            ntmp = 0; // use first index into value array for
                        }
                        // this variable
                    }

                    // parameter doesn't have boundaries or groups specified
                    if (!_rng[jj] && !_grp[jj])
                    {
                        outl += _map[jj][ntmp];
                    } else
                    {
                        //for (int b = 0; b < _ncols; b++) {
                        // parameter has boundaries specified
                        if (_bnd[jj] != null)
                        {
                            for (int r = 0; r <= _bnd[jj].length; r++)
                            {
                                if (ntmp == r)
                                {
                                    if (r == 0)
                                    {
                                        outl += "[value <" + _bnd[jj][r] + "]";
                                        break;
                                    }
                                    if (r > 0 && r < _bnd[jj].length)
                                    {
                                        outl += "[" + _bnd[jj][r - 1] + "<= value <" + _bnd[jj][r] + "]";
                                        break;
                                    }
                                    if (r == _bnd[jj].length)
                                    {
                                        outl += "[value >=" + _bnd[jj][r - 1] + "]";
                                        break;
                                    }
                                }
                            }
                        }
                        // parameter has groups specified
                        else if (_group[jj] != null)
                        {
                            for (int r = 0; r <= _group[jj].length; r++)
                            {
                                if (ntmp == r)
                                {
                                    outl += "Group [" + r + "] Values [" + _group[jj][r] + "]";
                                    break;
                                }
                            }
                        }
                        //}
                    }

                    if (jj < _ncols - 1)
                    {
                        outl += ",";
                    }
                }

                write(_fileNameMissing, outl);
            }

        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    public synchronized void updateTwoWay(int st, int num_rows, int[][] test, int start, int end)
    {
        //_test = test;
        _nrows = num_rows;

        long n_tot_tway_cov = _n_tot_tway_cov;
        int i, j, ni, nj, m;
        long[] varvalStats2 = new long[NBINS + 1];
        long n_varvalconfigs2 = 0;


        // solver for invalid combinations
        CSolver validcomb = new CSolver();
        validcomb.SetConstraints(_constraints);
        validcomb.SetParameter(_parameters);
        /*
         * -Calculates the number of t-way combinations between parameters
         *
         * -Calculates the total variable value configurations
         */
        // Process the tests
        for (i = start; i < end; i++)
        {
            for (j = i + 1; j < _ncols; j++)
            {
                // nComs++; //number of combinations
                String temp_key = String.format("%s(%d,%d)", _tway, i, j);
                //String temp_key = _tway + "(" + String.valueOf(i) + "," + String.valueOf(j) + ")";
                int[][] comcount = comcount_array2.get(temp_key);
                // forall t-way combinations of input variable values:
                // comcount i,j == 0
                // for the combination designated by i,j increment counts
                for (m = st; m < _nrows; m++)
                {
                    int v1 = test[m][i];
                    int v2 = test[m][j];
                    String[][] pars = new String[2][];
                    pars[0] = new String[2];
                    pars[1] = new String[2];

                    pars[0][0] = _parameters.get(i).getName();
                    pars[1][0] = _parameters.get(j).getName();

                    pars[0][1] = _parameters.get(i).getValues().get(v1);
                    pars[1][1] = _parameters.get(j).getValues().get(v2);

                    if (v1 < comcount.length)
                    {
                        if (v2 < comcount[v1].length)
                        {
                            if (comcount[v1][v2] == 1 || comcount[v1][v2] == -1)
                            {
                                //Combination is already in the set
                                //if(comcount[_test[m][i]][_test[m][j]] == -1)
                                continue;
                            }
                            if (_constraints.size() > 0)
                            {
                                if (validcomb.EvaluateCombination(pars))
                                {
                                    comcount[v1][v2] = 2;
                                } else
                                {
                                    comcount[v1][v2] = -2;
                                }
                                // flag invalid var-val config in set test
                            } else
                            {
                                comcount[v1][v2] = 2; // flag var-val config in set test
                            }
                        }
                    }
                }

                int varval_cnt = 0;
                int invalidcomb = 0;
                int invalidcombNotCovered = 0;

                for (ni = 0; ni < _nvals[i]; ni++)
                {
                    for (nj = 0; nj < _nvals[j]; nj++)
                    {
                        // count how many value var-val configs are contained in
                        // a test
                        if (comcount[ni][nj] == 1)
                        {
                            varval_cnt++;
                        }
                        if (comcount[ni][nj] == 2)
                        {
                            varval_cnt++;
                            n_tot_tway_cov++;
                            comcount[ni][nj] = 1;
                        } else
                        {
                            String[][] pars = new String[2][];
                            pars[0] = new String[2];
                            pars[1] = new String[2];

                            pars[0][0] = _parameters.get(i).getName();
                            pars[1][0] = _parameters.get(j).getName();

                            pars[0][1] = _parameters.get(i).getValues().get(ni);
                            pars[1][1] = _parameters.get(j).getValues().get(nj);

                            // count how many invalid configs are contained in
                            // the test

                            if (comcount[ni][nj] == -1)
                            {
                                invalidcomb++;
                            }
                            if (comcount[ni][nj] == -2)
                            {
                                invalidcomb += 1;
                                _aInvalidComb.add(pars);
                                comcount[ni][nj] = -1;
                                _aInvalidNotIn.remove(pars);
                            }
                            if (comcount[ni][nj] == -3)
                            {
                                invalidcombNotCovered += 1;
                            }


                        }

                    }
                }

                comcount_array2.put(temp_key, comcount);

                n_varvalconfigs2 = _nvals[i] * _nvals[j];
                n_varvalconfigs2 -= (invalidcomb + invalidcombNotCovered);
                double varval_cov = (double) varval_cnt / (double) n_varvalconfigs2;

                double varval = (double) varval_cnt / (double) varvaltotal;

                sumcov += varval;

                for (int b = 0; b <= NBINS; b++)
                {
                    if (varval_cov >= (double) b / (double) NBINS)
                    {
                        varvalStats2[b]++;
                    }
                }

                // now determine color for heat map display
                if (varval_cov < 0.2)
                {
                    hm_colors2[i][j] = 0;
                } else if (varval_cov < 0.4)
                {
                    hm_colors2[i][j] = 1;
                } else if (varval_cov < 0.6)
                {
                    hm_colors2[i][j] = 2;
                } else if (varval_cov < 0.8)
                {
                    hm_colors2[i][j] = 3;
                } else
                {
                    hm_colors2[i][j] = 4;
                }

            }

        }
        _n_tot_tway_cov = n_tot_tway_cov;
        _varvalStatN = varvalStats2;

        initialized = true;
    }

    public synchronized void updateThreeWay(int st, int num_rows, int[][] test, int start, int end)
    {

        //_test = test;
        _nrows = num_rows;

        long n_tot_tway_cov = _n_tot_tway_cov;
        int i, j, k, ni, nj, nk, m;
        long[] varvalStats3 = new long[NBINS + 1];
        long n_varvalconfigs3 = 0;


        // solver for invalid combinations
        CSolver validcomb = new CSolver();
        validcomb.SetConstraints(_constraints);
        validcomb.SetParameter(_parameters);


        for (i = 0; i < NBINS + 1; i++)
        {
            varvalStats3[i] = 0;
        }


        for (i = start; i < end; i++)
        {
            for (j = i + 1; j < _ncols - 1; j++)
            {
                for (k = j + 1; k < _ncols; k++)
                {
                    String temp_key = String.format("%s(%d,%d,%d)", _tway, i, j, k);
//                    String temp_key = _tway
//                                      + "("
//                                      + String.valueOf(i)
//                                      + ","
//                                      + String.valueOf(j)
//                                      + ","
//                                      + String.valueOf(
//                            k
//                                                      )
//                                      + ")";
                    int[][][] comcount = comcount_array3.get(temp_key);
                    // forall t-way combinations of input variable values:

                    // comcount i,j == 0
                    // for the combination designated by i,j increment counts
                    for (m = st; m < _nrows; m++)
                    { // mark if the var-val config
                        // is covered by the tests
                        int v1 = test[m][i];
                        int v2 = test[m][j];
                        int v3 = test[m][k];
                        String[][] pars = new String[3][];
                        pars[0] = new String[2];
                        pars[1] = new String[2];
                        pars[2] = new String[2];

                        pars[0][0] = _parameters.get(i).getName();
                        pars[1][0] = _parameters.get(j).getName();
                        pars[2][0] = _parameters.get(k).getName();

                        pars[0][1] = _parameters.get(i).getValues().get(v1);
                        pars[1][1] = _parameters.get(j).getValues().get(v2);
                        pars[2][1] = _parameters.get(k).getValues().get(v3);
                        if (v1 < comcount.length)
                        {
                            if (v2 < comcount[v1].length)
                            {
                                if (v3 < comcount[v1][v2].length)
                                {
                                    if (comcount[v1][v2][v3] == 1
                                        || comcount[v1][v2][v3] == -1)
                                    {
                                        //Combination is already in the set
                                        continue;
                                    }

                                    if (_constraints.size() > 0)
                                    {
                                        if (validcomb.EvaluateCombination(pars))
                                        {
                                            comcount[v1][v2][v3] = 2;
                                        } else
                                        {
                                            comcount[v1][v2][v3] = -2;
                                        }

                                    } else
                                    {
                                        comcount[v1][v2][v3] = 2;
                                    }
                                }
                            }
                        }
                    }

                    int varval_cnt = 0;
                    int invalidComb = 0;
                    int invalidcombNotCovered = 0;

                    for (ni = 0; ni < _nvals[i]; ni++)
                    {
                        for (nj = 0; nj < _nvals[j]; nj++)
                        {
                            for (nk = 0; nk < _nvals[k]; nk++)
                            {
                                if (comcount[ni][nj][nk] == 1)
                                {
                                    varval_cnt++;
                                }
                                if (comcount[ni][nj][nk] == 2)
                                {
                                    varval_cnt++;
                                    n_tot_tway_cov++;
                                    comcount[ni][nj][nk] = 1;
                                } // count valid configs in set test
                                else
                                {
                                    String[][] pars = new String[3][];
                                    pars[0] = new String[2];
                                    pars[1] = new String[2];
                                    pars[2] = new String[2];

                                    pars[0][0] = _parameters.get(i).getName();
                                    pars[1][0] = _parameters.get(j).getName();
                                    pars[2][0] = _parameters.get(k).getName();

                                    pars[0][1] = _parameters.get(i).getValues().get(ni);
                                    pars[1][1] = _parameters.get(j).getValues().get(nj);
                                    pars[2][1] = _parameters.get(k).getValues().get(nk);

                                    if (comcount[ni][nj][nk] == -1)
                                    {
                                        invalidComb++;
                                    }
                                    if (comcount[ni][nj][nk] == -2)
                                    {
                                        invalidComb += 1;
                                        _aInvalidComb.add(pars);
                                        comcount[ni][nj][nk] = -1;
                                        _aInvalidNotIn.remove(pars);
                                    }
                                    if (comcount[ni][nj][nk] == -3)
                                    {
                                        invalidcombNotCovered += 1;
                                    }

                                }
                            }
                        }
                    }

                    comcount_array3.put(temp_key, comcount);

                    n_varvalconfigs3 = _nvals[i] * _nvals[j] * _nvals[k];
                    n_varvalconfigs3 -= (invalidComb + invalidcombNotCovered);
                    double varval_cov = (double) varval_cnt / (double) n_varvalconfigs3;

                    double varval = (double) varval_cnt / (double) varvaltotal;

                    sumcov += varval;
                    // varval_cov bins give the number of var/val configurations
                    // covered
                    // at the levels: 0, [5,10), [10,15) etc. (assume 20 bins)
                    for (int b = 0; b <= NBINS; b++)
                    {
                        if (varval_cov >= (double) b / (double) NBINS)
                        {
                            varvalStats3[b]++;
                        }
                    }

                }
            }

        }

        _n_tot_tway_cov = n_tot_tway_cov;
        _varvalStatN = varvalStats3;

        initialized = true;

    }

    public synchronized void updateFourWay(int st, int num_rows, int[][] test, int start, int end)
    {

        int i, j, k, r, ni, nj, nk, nr, m;
        long[] varvalStats4 = new long[NBINS + 1];
        long n_varvalconfigs4;
        //_test = test;
        _nrows = num_rows;

        long n_tot_tway_cov = _n_tot_tway_cov;


        // solver for invalid combinations
        CSolver validcomb = new CSolver();
        validcomb.SetConstraints(_constraints);
        validcomb.SetParameter(_parameters);


        for (i = 0; i < NBINS + 1; i++)
        {
            varvalStats4[i] = 0;
        }

        for (i = start; i < end; i++)
        {
            for (j = i + 1; j < _ncols - 2; j++)
            {
                for (k = j + 1; k < _ncols - 1; k++)
                {
                    for (r = k + 1; r < _ncols; r++)
                    {
                        String temp_key = String.format("%s(%d,%d,%d,%d)", _tway, i, j, k, r);
//                        String temp_key = _tway + "(" + String.valueOf(i) + "," + String.valueOf(j) + "," + String
//                                .valueOf(k) +
//                                          "," + String.valueOf(r) + ")";
                        int[][][][] comcount = comcount_array4.get(temp_key);

                        // forall t-way combinations of input variable values:
                        // comcount i,j == 0
                        // for the combination designated by i,j increment
                        // counts
                        for (m = st; m < _nrows; m++)
                        {
                            int v1 = test[m][i];
                            int v2 = test[m][j];
                            int v3 = test[m][k];
                            int v4 = test[m][r];
                            String[][] pars = new String[4][];
                            pars[0] = new String[2];
                            pars[1] = new String[2];
                            pars[2] = new String[2];
                            pars[3] = new String[2];

                            pars[0][0] = _parameters.get(i).getName();
                            pars[1][0] = _parameters.get(j).getName();
                            pars[2][0] = _parameters.get(k).getName();
                            pars[3][0] = _parameters.get(r).getName();

                            pars[0][1] = _parameters.get(i).getValues().get(v1);
                            pars[1][1] = _parameters.get(j).getValues().get(v2);
                            pars[2][1] = _parameters.get(k).getValues().get(v3);
                            pars[3][1] = _parameters.get(r).getValues().get(v4);
                            if (v1 < comcount.length)
                            {
                                if (v2 < comcount[v1].length)
                                {
                                    if (v3 < comcount[v1][v2].length)
                                    {
                                        if (v4 < comcount[v1][v2][v3].length)
                                        {
                                            if (comcount[v1][v2][v3][v4] == 1 ||
                                                comcount[v1][v2][v3][v4] == -1)
                                            {
                                                //Combination is already in the set
                                                continue;
                                            }

                                            if (_constraints.size() > 0)
                                            {
                                                if (validcomb.EvaluateCombination(pars))
                                                {
                                                    comcount[v1][v2][v3][v4] = 2;
                                                } else
                                                {
                                                    comcount[v1][v2][v3][v4] = -2;
                                                }

                                            } else
                                            {
                                                comcount[v1][v2][v3][v4] = 2;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        int varval_cnt = 0;
                        int invalidComb = 0;
                        int invalidcombNotCovered = 0;
                        // count how many value configs are contained in a test
                        for (ni = 0; ni < _nvals[i]; ni++)
                        {
                            for (nj = 0; nj < _nvals[j]; nj++)
                            {
                                for (nk = 0; nk < _nvals[k]; nk++)
                                {
                                    for (nr = 0; nr < _nvals[r]; nr++)
                                    {
                                        if (comcount[ni][nj][nk][nr] == 1)
                                        {
                                            varval_cnt++;
                                        }
                                        if (comcount[ni][nj][nk][nr] == 2)
                                        {
                                            varval_cnt++;
                                            n_tot_tway_cov++;
                                            comcount[ni][nj][nk][nr] = 1;
                                        } else
                                        {
                                            String[][] pars = new String[4][];
                                            pars[0] = new String[2];
                                            pars[1] = new String[2];
                                            pars[2] = new String[2];
                                            pars[3] = new String[2];

                                            pars[0][0] = _parameters.get(i).getName();
                                            pars[1][0] = _parameters.get(j).getName();
                                            pars[2][0] = _parameters.get(k).getName();
                                            pars[3][0] = _parameters.get(r).getName();

                                            pars[0][1] = _parameters.get(i).getValues().get(ni);
                                            pars[1][1] = _parameters.get(j).getValues().get(nj);
                                            pars[2][1] = _parameters.get(k).getValues().get(nk);
                                            pars[3][1] = _parameters.get(r).getValues().get(nr);

                                            if (comcount[ni][nj][nk][nr] == -1)
                                            {
                                                invalidComb++;
                                            }
                                            if (comcount[ni][nj][nk][nr] == -2)
                                            {
                                                invalidComb += 1;
                                                _aInvalidComb.add(pars);
                                                comcount[ni][nj][nk][nr] = -1;
                                                _aInvalidNotIn.remove(pars);
                                            }
                                            if (comcount[ni][nj][nk][nr] == -3)
                                            {
                                                invalidcombNotCovered += 1;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        n_varvalconfigs4 = _nvals[i] * _nvals[j] * _nvals[k] * _nvals[r];
                        n_varvalconfigs4 -= (invalidComb + invalidcombNotCovered);
                        double varval_cov = (double) varval_cnt / (double) n_varvalconfigs4;
                        double varval = (double) varval_cnt / (double) varvaltotal;

                        sumcov += varval;
                        // varval_cov bins give the number of var/val
                        // configurations covered
                        // at the levels: 0, [5,10), [10,15) etc. (assume 20
                        // bins)
                        for (int b = 0; b <= NBINS; b++)
                        {
                            if (varval_cov >= (double) b / (double) NBINS)
                            {
                                varvalStats4[b]++;
                            }
                        }

                    }
                }
            }

        }

        _n_tot_tway_cov = n_tot_tway_cov;
        _varvalStatN = varvalStats4;

        initialized = true;

    }

    public synchronized void updateFiveWay(int st, int num_rows, int[][] test, int start, int end)
    {

        int i, j, k, r, x, ni, nj, nk, nr, nx, m;
        long[] varvalStats5 = new long[NBINS + 1];
        long n_varvalconfigs5;

        for (i = 0; i < NBINS + 1; i++)
        {
            varvalStats5[i] = 0;
        }

        //_test = test;
        _nrows = num_rows;

        long n_tot_tway_cov = _n_tot_tway_cov;


        // solver for invalid combinations
        CSolver validcomb = new CSolver();
        validcomb.SetConstraints(_constraints);
        validcomb.SetParameter(_parameters);

        for (i = start; i < end; i++)
        {
            for (j = i + 1; j < _ncols - 3; j++)
            {
                for (k = j + 1; k < _ncols - 2; k++)
                {
                    for (r = k + 1; r < _ncols - 1; r++)
                    {

                        for (x = r + 1; x < _ncols; x++)
                        {
                            String temp_key = String.format("%s(%d,%d,%d,%d,%d)", _tway, i, j, k, r, x);
//                            String temp_key = _tway + "(" + String.valueOf(i) + "," + String.valueOf(j) + "," + String
//                                    .valueOf(k) +
//                                              "," + String.valueOf(r) + "," + String.valueOf(x) + ")";

                            int[][][][][] comcount = comcount_array5.get(temp_key);

                            // forall t-way combinations of input variable
                            // values:

                            // comcount i,j == 0
                            // for the combination designated by i,j increment
                            // counts
                            for (m = st; m < _nrows; m++)
                            {
                                // mark if the var-val config is covered by the tests
                                // comcount[_test[m][i]][_test[m][j]][_test[m][k]][_test[m][r]][_test[m][x]] += 1;
                                // coumcount i,j == 1 iff some tests contains tuple i,j
                                int v1 = test[m][i];
                                int v2 = test[m][j];
                                int v3 = test[m][k];
                                int v4 = test[m][r];
                                int v5 = test[m][x];
                                String[][] pars = new String[5][];
                                pars[0] = new String[2];
                                pars[1] = new String[2];
                                pars[2] = new String[2];
                                pars[3] = new String[2];
                                pars[4] = new String[2];

                                pars[0][0] = _parameters.get(i).getName();
                                pars[1][0] = _parameters.get(j).getName();
                                pars[2][0] = _parameters.get(k).getName();
                                pars[3][0] = _parameters.get(r).getName();
                                pars[4][0] = _parameters.get(x).getName();

                                pars[0][1] = _parameters.get(i).getValues().get(v1);
                                pars[1][1] = _parameters.get(j).getValues().get(v2);
                                pars[2][1] = _parameters.get(k).getValues().get(v3);
                                pars[3][1] = _parameters.get(r).getValues().get(v4);
                                pars[4][1] = _parameters.get(x).getValues().get(v5);
                                if (v1 < comcount.length)
                                {
                                    if (v2 < comcount[v1].length)
                                    {
                                        if (v3 < comcount[v1][v2].length)
                                        {
                                            if (v4 < comcount[v1][v2][v3].length)
                                            {
                                                if (v5 < comcount[v1][v2][v3][v4].length)
                                                {
                                                    if (comcount[v1][v2][v3][v4][v5] == 1 ||
                                                        comcount[v1][v2][v3][v4][v5] == -1)
                                                    {
                                                        //Combination is already in the set
                                                        continue;
                                                    }

                                                    if (_constraints.size() > 0)
                                                    {
                                                        if (validcomb.EvaluateCombination(pars))
                                                        {
                                                            comcount[v1][v2][v3][v4][v5] = 2;
                                                        } else
                                                        {
                                                            comcount[v1][v2][v3][v4][v5] = -2;
                                                        }
                                                    } else
                                                    {
                                                        comcount[v1][v2][v3][v4][v5] = 2;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            int varval_cnt = 0;
                            int invalidComb = 0;
                            int invalidcombNotCovered = 0;
                            // count how many value configs are contained in a
                            // test
                            for (ni = 0; ni < _nvals[i]; ni++)
                            {
                                for (nj = 0; nj < _nvals[j]; nj++)
                                {
                                    for (nk = 0; nk < _nvals[k]; nk++)
                                    {
                                        for (nr = 0; nr < _nvals[r]; nr++)
                                        {
                                            for (nx = 0; nx < _nvals[x]; nx++)
                                            {
                                                if (comcount[ni][nj][nk][nr][nx] == 1)
                                                {
                                                    varval_cnt++;
                                                }
                                                if (comcount[ni][nj][nk][nr][nx] == 2)
                                                {
                                                    varval_cnt++;
                                                    n_tot_tway_cov++;
                                                    comcount[ni][nj][nk][nr][nx] = 1;
                                                } else
                                                {
                                                    String[][] pars = new String[5][];
                                                    pars[0] = new String[2];
                                                    pars[1] = new String[2];
                                                    pars[2] = new String[2];
                                                    pars[3] = new String[2];
                                                    pars[4] = new String[2];

                                                    pars[0][0] = _parameters.get(i).getName();
                                                    pars[1][0] = _parameters.get(j).getName();
                                                    pars[2][0] = _parameters.get(k).getName();
                                                    pars[3][0] = _parameters.get(r).getName();
                                                    pars[4][0] = _parameters.get(x).getName();

                                                    pars[0][1] = _parameters.get(i).getValues().get(ni);
                                                    pars[1][1] = _parameters.get(j).getValues().get(nj);
                                                    pars[2][1] = _parameters.get(k).getValues().get(nk);
                                                    pars[3][1] = _parameters.get(r).getValues().get(nr);
                                                    pars[4][1] = _parameters.get(x).getValues().get(nx);

                                                    if (comcount[ni][nj][nk][nr][nx] == -1)
                                                    {
                                                        invalidComb++;
                                                    }
                                                    if (comcount[ni][nj][nk][nr][nx] == -2)
                                                    {
                                                        invalidComb += 1;
                                                        _aInvalidComb.add(pars);
                                                        comcount[ni][nj][nk][nr][nx] = -1;
                                                        _aInvalidNotIn.remove(pars);
                                                    }
                                                    if (comcount[ni][nj][nk][nr][nx] == -3)
                                                    {
                                                        invalidcombNotCovered += 1;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            comcount_array5.put(temp_key, comcount);
                            n_varvalconfigs5 = _nvals[i] * _nvals[j] * _nvals[k] * _nvals[r] * _nvals[x];
                            n_varvalconfigs5 -= (invalidComb + invalidcombNotCovered);
                            double varval = (double) varval_cnt / (double) varvaltotal;

                            sumcov += varval;
                            double varval_cov = (double) varval_cnt / (double) n_varvalconfigs5;
                            // varval_cov bins give the number of var/val
                            // configurations covered
                            // at the levels: 0, [5,10), [10,15) etc. (assume 20
                            // bins)
                            for (int b = 0; b <= NBINS; b++)
                            {
                                if (varval_cov >= (double) b / (double) NBINS)
                                {
                                    varvalStats5[b]++;
                                }
                            }

                        }
                    }
                }
            }

        }

        _n_tot_tway_cov = n_tot_tway_cov;
        _varvalStatN = varvalStats5;

        initialized = true;

    }

    public synchronized void updateSixWay(int st, int num_rows, int[][] test, int start, int end)
    {

        int i, j, k, r, x, z, ni, nj, nk, nr, nx, nz, m;
        long[] varvalStats6 = new long[NBINS + 1];
        long n_varvalconfigs6;


        for (i = 0; i < NBINS + 1; i++)
        {
            varvalStats6[i] = 0;
        }

        //_test = test;
        _nrows = num_rows;

        long n_tot_tway_cov = _n_tot_tway_cov;


        // solver for invalid combinations
        CSolver validcomb = new CSolver();
        validcomb.SetConstraints(_constraints);
        validcomb.SetParameter(_parameters);

        for (i = start; i < end; i++)
        {

            for (j = i + 1; j < _ncols - 4; j++)
            {
                for (k = j + 1; k < _ncols - 3; k++)
                {
                    for (r = k + 1; r < _ncols - 2; r++)
                    {

                        for (x = r + 1; x < _ncols - 1; x++)
                        {
                            for (z = x + 1; z < _ncols; z++)
                            {
                                String temp_key = String.format("%s(%d,%d,%d,%d,%d,%d)", _tway, i, j, k, r, x, z);

//                                String temp_key = _tway
//                                                  + "("
//                                                  + String.valueOf(i)
//                                                  + ","
//                                                  + String.valueOf(j)
//                                                  + ","
//                                                  + String.valueOf(k)
//                                                  +
//                                                  ","
//                                                  + String.valueOf(r)
//                                                  + ","
//                                                  + String.valueOf(x)
//                                                  + ","
//                                                  + String.valueOf(z)
//                                                  + ")";
                                int[][][][][][] comcount = comcount_array6.get(temp_key);
                                // forall t-way combinations of input variable
                                // values:
                                //

                                // comcount i,j == 0
                                // for the combination designated by i,j
                                // increment counts
                                for (m = st; m < _nrows; m++)
                                { // mark if the
                                    // var-val
                                    // config is
                                    // covered by
                                    // the tests
                                    // comcount[_test[m][i]][_test[m][j]][_test[m][k]][_test[m][r]][_test[m][x]][_test[m][z]]
                                    // += 1;
                                    // coumcount i,j
                                    // == 1 iff some
                                    // tests
                                    // contains
                                    // tuple i,j
                                    int v1 = test[m][i];
                                    int v2 = test[m][j];
                                    int v3 = test[m][k];
                                    int v4 = test[m][r];
                                    int v5 = test[m][x];
                                    int v6 = test[m][z];
                                    String[][] pars = new String[6][];
                                    pars[0] = new String[2];
                                    pars[1] = new String[2];
                                    pars[2] = new String[2];
                                    pars[3] = new String[2];
                                    pars[4] = new String[2];
                                    pars[5] = new String[2];

                                    pars[0][0] = _parameters.get(i).getName();
                                    pars[1][0] = _parameters.get(j).getName();
                                    pars[2][0] = _parameters.get(k).getName();
                                    pars[3][0] = _parameters.get(r).getName();
                                    pars[4][0] = _parameters.get(x).getName();
                                    pars[5][0] = _parameters.get(z).getName();

                                    pars[0][1] = _parameters.get(i).getValues().get(v1);
                                    pars[1][1] = _parameters.get(j).getValues().get(v2);
                                    pars[2][1] = _parameters.get(k).getValues().get(v3);
                                    pars[3][1] = _parameters.get(r).getValues().get(v4);
                                    pars[4][1] = _parameters.get(x).getValues().get(v5);
                                    pars[5][1] = _parameters.get(z).getValues().get(v6);
                                    if (v1 < comcount.length)
                                    {
                                        if (v2 < comcount[v1].length)
                                        {
                                            if (v3 < comcount[v1][v2].length)
                                            {
                                                if (v4 < comcount[v1][v2][v3].length)
                                                {
                                                    if (v5 < comcount[v1][v2][v3][v4].length)
                                                    {
                                                        if (v6 < comcount[v1][v2][v3][v4][v5].length)
                                                        {
                                                            if (comcount[v1][v2][v3][v4][v5][v6] == 1 ||
                                                                comcount[v1][v2][v3][v4][v5][v6] == -1)
                                                            {
                                                                //Combination is already in the set
                                                                continue;
                                                            }

                                                            if (_constraints.size() > 0)
                                                            {
                                                                if (validcomb.EvaluateCombination(pars))
                                                                {
                                                                    comcount[v1][v2][v3][v4][v5][v6] = 2;
                                                                } else
                                                                {
                                                                    comcount[v1][v2][v3][v4][v5][v6] = -2;
                                                                }

                                                            } else
                                                            {
                                                                comcount[v1][v2][v3][v4][v5][v6] = 2;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                int varval_cnt = 0;
                                int invalidComb = 0;
                                int invalidcombNotCovered = 0;
                                // count how many value configs are contained in
                                // a test
                                for (ni = 0; ni < _nvals[i]; ni++)
                                {
                                    for (nj = 0; nj < _nvals[j]; nj++)
                                    {
                                        for (nk = 0; nk < _nvals[k]; nk++)
                                        {
                                            for (nr = 0; nr < _nvals[r]; nr++)
                                            {
                                                for (nx = 0; nx < _nvals[x]; nx++)
                                                {
                                                    for (nz = 0; nz < _nvals[z]; nz++)
                                                    {
                                                        if (comcount[ni][nj][nk][nr][nx][nz] == 1)
                                                        {
                                                            varval_cnt++;
                                                        }
                                                        if (comcount[ni][nj][nk][nr][nx][nz] == 2)
                                                        {
                                                            varval_cnt++;
                                                            n_tot_tway_cov++;
                                                            comcount[ni][nj][nk][nr][nx][nz] = 1;
                                                        } else
                                                        {
                                                            String[][] pars = new String[6][];
                                                            pars[0] = new String[2];
                                                            pars[1] = new String[2];
                                                            pars[2] = new String[2];
                                                            pars[3] = new String[2];
                                                            pars[4] = new String[2];
                                                            pars[5] = new String[2];

                                                            pars[0][0] = _parameters.get(i).getName();
                                                            pars[1][0] = _parameters.get(j).getName();
                                                            pars[2][0] = _parameters.get(k).getName();
                                                            pars[3][0] = _parameters.get(r).getName();
                                                            pars[4][0] = _parameters.get(x).getName();
                                                            pars[5][0] = _parameters.get(z).getName();

                                                            pars[0][1] = _parameters.get(i).getValues().get(ni);
                                                            pars[1][1] = _parameters.get(j).getValues().get(nj);
                                                            pars[2][1] = _parameters.get(k).getValues().get(nk);
                                                            pars[3][1] = _parameters.get(r).getValues().get(nr);
                                                            pars[4][1] = _parameters.get(x).getValues().get(nx);
                                                            pars[5][1] = _parameters.get(z).getValues().get(nz);

                                                            if (comcount[ni][nj][nk][nr][nx][nz] == -1)
                                                            {
                                                                invalidComb++;
                                                            }
                                                            if (comcount[ni][nj][nk][nr][nx][nz] == -2)
                                                            {
                                                                invalidComb += 1;
                                                                _aInvalidComb.add(pars);
                                                                comcount[ni][nj][nk][nr][nx][nz] = -1;
                                                                _aInvalidNotIn.remove(pars);
                                                            }
                                                            if (comcount[ni][nj][nk][nr][nx][nz] == -3)
                                                            {
                                                                invalidcombNotCovered += 1;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                comcount_array6.put(temp_key, comcount);
                                n_varvalconfigs6 = _nvals[i] * _nvals[j] * _nvals[k] * _nvals[r] * _nvals[x]
                                                   * _nvals[z];
                                n_varvalconfigs6 -= (invalidComb + invalidcombNotCovered);
                                double varval = (double) varval_cnt / (double) varvaltotal;

                                sumcov += varval;
                                double varval_cov = (double) varval_cnt / (double) n_varvalconfigs6;
                                // varval_cov bins give the number of var/val
                                // configurations covered
                                // at the levels: 0, [5,10), [10,15) etc.
                                // (assume 20 bins)
                                for (int b = 0; b <= NBINS; b++)
                                {
                                    if (varval_cov >= (double) b / (double) NBINS)
                                    {
                                        varvalStats6[b]++;
                                    }
                                }


                            }
                        }
                    }
                }
            }

        }

        _n_tot_tway_cov = n_tot_tway_cov;
        _varvalStatN = varvalStats6;

        initialized = true;
    }


}
