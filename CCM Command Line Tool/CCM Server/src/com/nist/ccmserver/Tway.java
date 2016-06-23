package com.nist.ccmserver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

import javax.swing.JProgressBar;

public class Tway extends RecursiveTask {
	private String _tway;
	private int _start;
	private int _end;
	private boolean _parallel;
	// JProgressBar _barra;
	private int _ncols;
	private int _nrows;
	private int[][] _test;

	private int[] _nvals;
	private String _fileNameMissing = null;
	private String _fileNameReport = null;
	private int _NGenTests;
	private boolean _GenTests;
	private double _minCov;
	private String[][] _map;
	private boolean _rptMissingCom;
	private int _MaxGenTests = 10000;
	private boolean _appendTests = false;
	private Boolean[] _rng;
	private double[][] _bnd;
	private Boolean[] _grp;
	private Object[][] _group;

	private boolean wait = false;

	private int _parmName = 0;

	private List<int[]> _missing = new ArrayList<int[]>();

	private List<Parameter> _parameters;
	private List<meConstraint> _constraints;

	private int NBINS = 20;

	int[][] out_test;
	int Nout_test;

	public Tway(String tway, int start, int end, int[][] test, int[] nvals, int nrows, int ncols,
			List<Parameter> parameters, List<meConstraint> constraints, String[][] map) {
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

	public int[][] hm_colors2;
	
	public long _n_tot_tway_cov;
	public long[] _varvalStatN;
	public long _nComs;
	public long _tot_varvalconfig;
	public List<String[][]> _aInvalidComb = null;
	public List<String[][]> _aInvalidNotIn = null;
	
	public String appendFile = "";

	// set properties

	public void set_wait(boolean w) {
		wait = w;
	}
	
	public void set_appendFile(String x){
		appendFile = x;
	}

	public void set_parmName(int p) {
		_parmName = p;
	}

	public void set_map(String[][] m) {
		_map = m;
	}

	public void set_bnd(double[][] d) {
		_bnd = d;
	}

	public void set_Rng(Boolean b[]) {
		_rng = b;
	}

	public void set_grp(Boolean b[]) {
		_grp = b;
	}

	public void set_group(Object[][] d) {
		_group = d;
	}

	public void set_appendTests(boolean b) {
		_appendTests = b;
	}

	public void set_MaxGenTests(int i) {
		_MaxGenTests = i;
	}

	public void set_minCov(double d) {
		_minCov = d;
	}

	public void set_rptMissingCom(boolean m) {
		_rptMissingCom = m;
	}

	public void set_GenTests(boolean b) {
		_GenTests = b;
	}

	public void set_NGenTests(int i) {
		_NGenTests = i;

	}

	public void set_FileNameMissing(String fn) {
		_fileNameMissing = fn;

	}

	public void set_FileNameReport(String fn) {
		_fileNameReport = fn;

	}

	public void set_Parallel(boolean b) {
		_parallel = b;
	}

	public List<String[][]> get_InvalidComb() {
		return _aInvalidComb;
	}

	public List<String[][]> get_InvalidNotin() {

		return _aInvalidNotIn;
	}

	// methods
	private void GetTway() {
		switch (_tway) {
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

	public static synchronized void write(String sFileName, String sContent) {
		try {

			File oFile = new File(sFileName);
			if (!oFile.exists()) {
				oFile.createNewFile();
			}
			if (oFile.canWrite()) {
				BufferedWriter oWriter = new BufferedWriter(new FileWriter(sFileName, true));
				oWriter.append(sContent);
				oWriter.newLine();
				oWriter.flush();
				oWriter.close();
			}

		} catch (IOException oException) {
			throw new IllegalArgumentException("Error appending/File cannot be written: \n" + sFileName);
		}
	}

	private void TwoWay() {

		long n_tot_tway_cov = 0;
		int i, j, ni, nj, m, ti;
		long[] varvalStats2 = new long[NBINS + 1];
		long n_varvalconfigs2;
		long nComs = 0; // number of combinations = C(ncols, t)
		long tot_varvalconfigs2 = 0;

		hm_colors2 = new int[_ncols][];
		for (i = 0; i < _ncols; i++) {
			hm_colors2[i] = new int[_ncols];
		}

		// prepare to create a new batch of generated tests to cover missing
		// tests

		for (i = 0; i < NBINS + 1; i++)
			varvalStats2[i] = 0;

		// solver for invalid combinations
		CSolver validcomb = new CSolver();
		validcomb.SetConstraints(_constraints);
		validcomb.SetParameter(_parameters);

		_aInvalidComb = new ArrayList<String[][]>();
		_aInvalidNotIn = new ArrayList<String[][]>();

		int varvaltotal = 0;

		/*
		 * -Calculates the number of t-way combinations between parameters
		 * 
		 * -Calculates the total variable value configurations
		 */
		for (i = _start; i < _end; i++) {
			for (j = i + 1; j < _ncols; j++) {
				nComs++;
				varvaltotal += (_nvals[i] * _nvals[j]);
			}
		}

		double div = (double) varvaltotal / (double) nComs;
		double sumcov = 0;
		// Process the tests
		for (i = _start; i < _end; i++) {
			for (j = i + 1; j < _ncols; j++) {
				// nComs++; //number of combinations
				int[][] comcount = new int[_nvals[i]][];
				for (ti = 0; ti < _nvals[i]; ti++) {
					comcount[ti] = new int[_nvals[j]];
				}
				// forall t-way combinations of input variable values:
				// comcount i,j == 0
				// for the combination designated by i,j increment counts
				for (m = 0; m < _nrows; m++) {

					String[][] pars = new String[2][];
					pars[0] = new String[2];
					pars[1] = new String[2];

					pars[0][0] = _parameters.get(i).getName();
					pars[1][0] = _parameters.get(j).getName();

					pars[0][1] = _parameters.get(i).getValues().get(_test[m][i]).toString();
					pars[1][1] = _parameters.get(j).getValues().get(_test[m][j]).toString();
					
					if (_constraints.size() > 0) {
						if (validcomb.EvaluateCombination(pars)){
							comcount[_test[m][i]][_test[m][j]] += 1;
						}
							 // flag
																		// valid
																		// var-val
																		// config
																		// in
																		// set
																		// test
						else
							comcount[_test[m][i]][_test[m][j]] -= 1; // flag
																		// invalid
																		// var-val
																		// config
																		// in
																		// set
																		// test
					} else{
						comcount[_test[m][i]][_test[m][j]] += 1; // flag var-val
						// config in
						// set test
					}


					// coumcount i,j == 1 iff some tests contains tuple i,j
				}

				int varval_cnt = 0;
				int invalidcomb = 0;
				int invalidcombNotCovered = 0;

				for (ni = 0; ni < _nvals[i]; ni++) {
					for (nj = 0; nj < _nvals[j]; nj++) {
						// count how many value var-val configs are contained in
						// a test
						if (comcount[ni][nj] > 0) {
							varval_cnt++;
							n_tot_tway_cov++;
						} else {

							String[][] pars = new String[2][];
							pars[0] = new String[2];
							pars[1] = new String[2];

							pars[0][0] = _parameters.get(i).getName();
							pars[1][0] = _parameters.get(j).getName();

							pars[0][1] = _parameters.get(i).getValues().get(ni);
							pars[1][1] = _parameters.get(j).getValues().get(nj);

							// count how many invalid configs are contained in
							// the test
							if (comcount[ni][nj] <= -1) {
								invalidcomb += 1;
								_aInvalidComb.add(pars);
							}
							if (comcount[ni][nj] == 0 && _constraints.size() > 0) {

								if (!validcomb.EvaluateCombination(pars)) {
									// count how many invalid configs are not
									// contained in the test
									invalidcombNotCovered += 1;
									comcount[ni][nj] = -2;
									_aInvalidNotIn.add(pars);
								}

							}

						}

					}
				}

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
					if (varval_cov >= (double) b / (double) NBINS)
						varvalStats2[b]++;

				// now determine color for heat map display
				if (varval_cov < 0.2)
					hm_colors2[i][j] = 0;
				else if (varval_cov < 0.4)
					hm_colors2[i][j] = 1;
				else if (varval_cov < 0.6)
					hm_colors2[i][j] = 2;
				else if (varval_cov < 0.8)
					hm_colors2[i][j] = 3;
				else
					hm_colors2[i][j] = 4;

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
				if (_GenTests && varval_cov < _minCov) {
					for (ni = 0; ni < _nvals[i]; ni++) {
						for (nj = 0; nj < _nvals[j]; nj++) {

							if (comcount[ni][nj] == 0) { // not covered; write
															// out combination
															// to cover
								if (_rptMissingCom) {
									String outl = i + "," + j + " = " + ni + "," + nj + " ||" + _map[i][ni] + ","
											+ _map[j][nj] + "\n";
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

			/*
			 * if (_barra.isIndeterminate()) _barra.setIndeterminate(false);
			 * 
			 * _barra.setValue(_barra.getValue()+1);
			 */
		}

		_n_tot_tway_cov = n_tot_tway_cov;
		_nComs = nComs;
		_tot_varvalconfig = tot_varvalconfigs2;
		_varvalStatN = varvalStats2;

	}

	private void ThreeWay() {
		long n_tot_tway_cov = 0; // drk121109
		int i, j, k, ni, nj, nk, m, ti, tj;
		long[] varvalStats3 = new long[NBINS + 1];
		long n_varvalconfigs3; // drk121109
		long nComs = 0; // number of combinations = C(ncols, t) //drk121109
		long tot_varvalconfigs3 = 0;

		

		for (i = 0; i < NBINS + 1; i++)
			varvalStats3[i] = 0;

		// solver for invalid combinations
		CSolver validcomb = new CSolver();
		validcomb.SetConstraints(_constraints);
		validcomb.SetParameter(_parameters);

		_aInvalidComb = new ArrayList<String[][]>();
		_aInvalidNotIn = new ArrayList<String[][]>();

		for (i = _start; i < _end; i++) {

			for (j = i + 1; j < _ncols - 1; j++) {
				for (k = j + 1; k < _ncols; k++) {

					nComs++;

					int[][][] comcount = new int[_nvals[i]][][]; // allow row 0
																	// for #
																	// values
																	// per parm
					for (ti = 0; ti < _nvals[i]; ti++) {
						comcount[ti] = new int[_nvals[j]][];
						for (tj = 0; tj < _nvals[j]; tj++) {
							comcount[ti][tj] = new int[_nvals[k]];
						}
					}

					// forall t-way combinations of input variable values:

					// comcount i,j == 0
					// for the combination designated by i,j increment counts
					for (m = 0; m < _nrows; m++) { // mark if the var-val config
													// is covered by the tests

						String[][] pars = new String[3][];
						pars[0] = new String[2];
						pars[1] = new String[2];
						pars[2] = new String[2];

						pars[0][0] = _parameters.get(i).getName();
						pars[1][0] = _parameters.get(j).getName();
						pars[2][0] = _parameters.get(k).getName();

						pars[0][1] = _parameters.get(i).getValues().get(_test[m][i]);
						pars[1][1] = _parameters.get(j).getValues().get(_test[m][j]);
						pars[2][1] = _parameters.get(k).getValues().get(_test[m][k]);

						if (_constraints.size() > 0) {
							if (validcomb.EvaluateCombination(pars))
								comcount[_test[m][i]][_test[m][j]][_test[m][k]] += 1;
							else
								comcount[_test[m][i]][_test[m][j]][_test[m][k]] += -1;

						} else
							comcount[_test[m][i]][_test[m][j]][_test[m][k]] += 1;
						// coumcount i,j == 1 iff some tests contains tuple i,j
					}

					int varval_cnt = 0;
					int invalidComb = 0;
					int invalidcombNotCovered = 0;

					for (ni = 0; ni < _nvals[i]; ni++) {
						for (nj = 0; nj < _nvals[j]; nj++) {
							for (nk = 0; nk < _nvals[k]; nk++) {
								if (comcount[ni][nj][nk] > 0) {
									varval_cnt++;
									n_tot_tway_cov++;
								} // count valid configs in set test
								else {
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

									if (comcount[ni][nj][nk] <= -1) {
										// count invalid configs in set test
										invalidComb += 1;
										_aInvalidComb.add(pars);
									}
									if (comcount[ni][nj][nk] == 0 && _constraints.size() > 0) {
										if (!validcomb.EvaluateCombination(pars)) {// count
																					// invalid
																					// configs
																					// not
																					// in
																					// set
																					// test
											invalidcombNotCovered += 1;
											_aInvalidNotIn.add(pars);
											comcount[ni][nj][nk] = -2;
										}

									}
								}
							}
						}
					}
					n_varvalconfigs3 = _nvals[i] * _nvals[j] * _nvals[k];
					n_varvalconfigs3 -= (invalidComb + invalidcombNotCovered);
					tot_varvalconfigs3 += n_varvalconfigs3;

					double varval_cov = (double) varval_cnt / (double) n_varvalconfigs3;
					// varval_cov bins give the number of var/val configurations
					// covered
					// at the levels: 0, [5,10), [10,15) etc. (assume 20 bins)
					for (int b = 0; b <= NBINS; b++)
						if (varval_cov >= (double) b / (double) NBINS)
							varvalStats3[b]++;
					// now determine color for heat map display
					// if (varval_cov < 0.2) hm_colors3[i][j][k] = 0;
					// else if (varval_cov < 0.4) hm_colors3[i][j][k] = 1;
					// else if (varval_cov < 0.6) hm_colors3[i][j][k] = 2;
					// else if (varval_cov < 0.8) hm_colors3[i][j][k] = 3;
					// else hm_colors3[i][j][k] = 4;
					// results += "Com " + i + "," + j + " = " + varval_cnt +
					// "/" + n_varvalconfigs + " = " + (float)((float)varval_cnt
					// / (float)n_varvalconfigs) + "\n";

					// For missing combinations

					if (_GenTests && varval_cov < _minCov) {
						for (ni = 0; ni < _nvals[i]; ni++) {
							for (nj = 0; nj < _nvals[j]; nj++) {
								for (nk = 0; nk < _nvals[k]; nk++) {
									if (comcount[ni][nj][nk] == 0) { // not
																		// covered;
																		// write
																		// out
																		// combination
																		// to
																		// cover
										if (_rptMissingCom) {
											String outl = i + "," + j + "," + k + " = " + ni + "," + nj + "," + nk
													+ " ||" + _map[i][ni] + "," + _map[j][nj] + "," + _map[k][nk]
													+ "\n";
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
			/*
			 * if (_barra.isIndeterminate()) _barra.setIndeterminate(false);
			 * _barra.setValue(_barra.getValue()+1);
			 */
		}

		_n_tot_tway_cov = n_tot_tway_cov;
		_nComs = nComs;
		_tot_varvalconfig = tot_varvalconfigs3;
		_varvalStatN = varvalStats3;

		/*
		 * if (_GenTests) try {
		 * 
		 * if (_appendTests) { // append new tests to original, so write out
		 * original first for (int ii = 0; ii < _nrows; ii++) { String outl =
		 * ""; for (int jj = 0; jj < _ncols; jj++) {
		 * 
		 * outl += _map[jj][_test[ii][jj]];
		 * 
		 * if (jj < _ncols - 1) outl += ","; } write(_fileNameMissing,outl);
		 * 
		 * } } for (int ii = 0; ii < Nout_test; ii++) { String outl = ""; for
		 * (int jj = 0; jj < _ncols; jj++) { int ntmp = out_test[ii][jj];
		 * String[][] parV; if (ntmp>0 && _constraints.size() > 0) { parV = new
		 * String[1][]; parV[0] = new String[2]; parV[0][0] =
		 * _parameters.get(jj).getName(); parV[0][1] =
		 * (String)_parameters.get(jj).getValues().get(ntmp);
		 * 
		 * if (!validcomb.EvaluateCombination(parV)) ntmp=-1; }
		 * 
		 * if (ntmp < 0) // output value from input test file mapped by index {
		 * if (_constraints.size() > 0) //if there are constraints the valid
		 * value will be search {
		 * 
		 * do { ntmp++; parV = new String[1][]; parV[0] = new String[2];
		 * parV[0][0] = _parameters.get(jj).getName(); parV[0][1] =
		 * (String)_parameters.get(jj).getValues().get(ntmp); } while
		 * (!validcomb.EvaluateCombination(parV)); } else ntmp = 0; // use first
		 * index into value array for this variable }
		 * 
		 * if (!_rng[jj] && !_grp[jj]) outl += _map[jj][ntmp]; else { for (int b
		 * = 0; b < _end; b++) { if (_bnd[b] != null) { for (int r = 0; r <=
		 * _bnd[b].length; r++) { if (ntmp == r) { if (r==0) { outl+="[value < "
		 * + _bnd[b][r] + "]"; break; } if (r>0 && r<_bnd[b].length) { outl +=
		 * "[" + _bnd[b][r-1] + "<= value <"+ _bnd[b][r] +"]"; break; } if
		 * (r==_bnd[b].length) { outl+="[value >="+ _bnd[b][r-1] +"]"; break; }
		 * } } } if (_group[b] != null) { for (int r = 0; r <= _group[b].length;
		 * r++) { if (ntmp==r) { outl+="Group [" + r + "] Values [" +
		 * _group[b][r] + "]"; break; } } } } } if (jj < _ncols - 1) outl +=
		 * ","; }
		 * 
		 * write(_fileNameMissing,outl); }
		 * 
		 * } catch(Exception ex) { throw ex; }
		 */

	}

	private void FourWay() {
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

		_aInvalidComb = new ArrayList<String[][]>();
		_aInvalidNotIn = new ArrayList<String[][]>();

		for (i = 0; i < NBINS + 1; i++)
			varvalStats4[i] = 0;

		for (i = _start; i < _end; i++) {
			for (j = i + 1; j < _ncols - 2; j++) {
				for (k = j + 1; k < _ncols - 1; k++) {
					for (r = k + 1; r < _ncols; r++) {

						nComs++;

						int[][][][] comcount = new int[_nvals[i]][][][]; // allow
																			// row
																			// 0
																			// for
																			// #
																			// values
																			// per
																			// parm
						for (ti = 0; ti < _nvals[i]; ti++) {
							comcount[ti] = new int[_nvals[j]][][];
							for (tj = 0; tj < _nvals[j]; tj++) {
								comcount[ti][tj] = new int[_nvals[k]][];
								for (tk = 0; tk < _nvals[k]; tk++) {
									comcount[ti][tj][tk] = new int[_nvals[r]];
								}
							}
						}

						// forall t-way combinations of input variable values:
						// comcount i,j == 0
						// for the combination designated by i,j increment
						// counts
						for (m = 0; m < _nrows; m++) {

							String[][] pars = new String[4][];
							pars[0] = new String[2];
							pars[1] = new String[2];
							pars[2] = new String[2];
							pars[3] = new String[2];

							pars[0][0] = _parameters.get(i).getName();
							pars[1][0] = _parameters.get(j).getName();
							pars[2][0] = _parameters.get(k).getName();
							pars[3][0] = _parameters.get(r).getName();

							pars[0][1] = _parameters.get(i).getValues().get(_test[m][i]);
							pars[1][1] = _parameters.get(j).getValues().get(_test[m][j]);
							pars[2][1] = _parameters.get(k).getValues().get(_test[m][k]);
							pars[3][1] = _parameters.get(r).getValues().get(_test[m][r]);

							if (_constraints.size() > 0) {
								if (validcomb.EvaluateCombination(pars))
									comcount[_test[m][i]][_test[m][j]][_test[m][k]][_test[m][r]] += 1;
								else
									comcount[_test[m][i]][_test[m][j]][_test[m][k]][_test[m][r]] += -1;

							} else
								comcount[_test[m][i]][_test[m][j]][_test[m][k]][_test[m][r]] += 1;

						}

						int varval_cnt = 0;
						int invalidComb = 0;
						int invalidcombNotCovered = 0;
						// count how many value configs are contained in a test
						for (ni = 0; ni < _nvals[i]; ni++) {
							for (nj = 0; nj < _nvals[j]; nj++) {
								for (nk = 0; nk < _nvals[k]; nk++) {
									for (nr = 0; nr < _nvals[r]; nr++) {
										if (comcount[ni][nj][nk][nr] > 0) {
											varval_cnt++;
											n_tot_tway_cov++;
										} else {
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

											if (comcount[ni][nj][nk][nr] <= -1) {
												invalidComb += 1;
												_aInvalidComb.add(pars);
											}

											if (comcount[ni][nj][nk][nr] == 0 && _constraints.size() > 0) {
												if (!validcomb.EvaluateCombination(pars)) {
													invalidcombNotCovered += 1;
													_aInvalidNotIn.add(pars);
													comcount[ni][nj][nk][nr] = -2;
												}
											}
										}
									}
								}
							}
						}
						n_varvalconfigs4 = _nvals[i] * _nvals[j] * _nvals[k] * _nvals[r];
						n_varvalconfigs4 -= (invalidComb + invalidcombNotCovered);
						tot_varvalconfigs4 += n_varvalconfigs4;

						double varval_cov = (double) varval_cnt / (double) n_varvalconfigs4;
						// varval_cov bins give the number of var/val
						// configurations covered
						// at the levels: 0, [5,10), [10,15) etc. (assume 20
						// bins)
						for (int b = 0; b <= NBINS; b++)
							if (varval_cov >= (double) b / (double) NBINS)
								varvalStats4[b]++;

						// *********** For missing combinations
						if (_GenTests && varval_cov < _minCov) {
							for (ni = 0; ni < _nvals[i]; ni++) {
								for (nj = 0; nj < _nvals[j]; nj++) {
									for (nk = 0; nk < _nvals[k]; nk++) {
										for (nr = 0; nr < _nvals[r]; nr++) {
											if (comcount[ni][nj][nk][nr] == 0) { // not
																					// covered;
																					// write
																					// out
																					// combination
																					// to
																					// cover
												if (_rptMissingCom) {
													String outl = i + "," + j + "," + k + "," + r + " = " + ni + ","
															+ nj + "," + nk + "," + nr + " ||" + _map[i][ni] + ","
															+ _map[j][nj] + "," + _map[k][nk] + "," + _map[r][nr]
															+ "\n";
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

												// value i may be covered but
												// not value j
												/*
												 * Boolean comout = false; int
												 * w; for (w = 0; w < Nout_test
												 * && !comout; w++) { if
												 * (out_test[w][i] < 0 &&
												 * out_test[w][j] < 0 &&
												 * out_test[w][k] < 0 &&
												 * out_test[w][r] < 0) {
												 * out_test[w][i] = ni;
												 * out_test[w][j] = nj;
												 * out_test[w][k] = nk;
												 * out_test[w][r] = nr; comout =
												 * true; } if (out_test[w][i] ==
												 * ni && out_test[w][j] < 0 &&
												 * out_test[w][k] < 0 &&
												 * out_test[w][r] < 0) {
												 * out_test[w][j] = nj;
												 * out_test[w][k] = nk;
												 * out_test[w][r] = nr; comout =
												 * true; } if (out_test[w][i] <
												 * 0 && out_test[w][j] == nj &&
												 * out_test[w][k] < 0 &&
												 * out_test[w][r] < 0) {
												 * out_test[w][i] = ni;
												 * out_test[w][k] = nk;
												 * out_test[w][r] = nr; comout =
												 * true; } if (out_test[w][i] <
												 * 0 && out_test[w][j] < 0 &&
												 * out_test[w][k] == nk &&
												 * out_test[w][r] < 0) {
												 * out_test[w][i] = ni;
												 * out_test[w][j] = nj;
												 * out_test[w][r] = nr; comout =
												 * true; }
												 * 
												 * if (out_test[w][i] < 0 &&
												 * out_test[w][j] < 0 &&
												 * out_test[w][k] < 0 &&
												 * out_test[w][r] == nr) {
												 * out_test[w][i] = ni;
												 * out_test[w][j] = nj;
												 * out_test[w][k] = nk; comout =
												 * true; }
												 * 
												 * if (out_test[w][i] == ni &&
												 * out_test[w][j] == nj &&
												 * out_test[w][k] == nk &&
												 * out_test[w][r] == nr) {
												 * comout = true; } } if
												 * (!comout && Nout_test <
												 * _MaxGenTests) { // com was
												 * not output to out_test[w] =
												 * new int[_ncols]; for (int ii
												 * = 0; ii < _ncols; ii++)
												 * out_test[w][ii] = -1;
												 * out_test[w][i] = ni;
												 * out_test[w][j] = nj;
												 * out_test[w][k] = nk;
												 * out_test[w][r] = nr;
												 * Nout_test++; }
												 */
											}

										}
									}

								}
							}
						}

						// ***************************************
					}
				}
			}
			/*
			 * if (_barra.isIndeterminate()) _barra.setIndeterminate(false);
			 * _barra.setValue(_barra.getValue()+1);
			 */
		}

		_n_tot_tway_cov = n_tot_tway_cov;
		_nComs = nComs;
		_tot_varvalconfig = tot_varvalconfigs4;
		_varvalStatN = varvalStats4;

		/*
		 * if (_GenTests) try {
		 * 
		 * if (_appendTests) { // append new tests to original, so write out
		 * original first for (int ii = 0; ii < _nrows; ii++) { String outl =
		 * ""; for (int jj = 0; jj < _ncols; jj++) { outl +=
		 * _map[jj][_test[ii][jj]];
		 * 
		 * if (jj < _ncols - 1) outl += ","; }
		 * 
		 * write(_fileNameMissing,outl);
		 * 
		 * 
		 * 
		 * } } for (int ii = 0; ii < Nout_test; ii++) { String outl = ""; for
		 * (int jj = 0; jj < _ncols; jj++) { int ntmp = out_test[ii][jj];
		 * String[][] parV; if (ntmp>0 && _constraints.size() > 0) { parV = new
		 * String[1][]; parV[0] = new String[2]; parV[0][0] =
		 * _parameters.get(jj).getName(); parV[0][1] =
		 * (String)_parameters.get(jj).getValues().get(ntmp);
		 * 
		 * if (!validcomb.EvaluateCombination(parV)) ntmp=-1; }
		 * 
		 * if (ntmp < 0) { if (_constraints.size() > 0) //if there are
		 * constraints the valid value will be search {
		 * 
		 * do { ntmp++; parV = new String[1][]; parV[0] = new String[2];
		 * parV[0][0] = _parameters.get(jj).getName(); parV[0][1] =
		 * (String)_parameters.get(jj).getValues().get(ntmp); } while
		 * (!validcomb.EvaluateCombination(parV)); } else ntmp = 0;
		 * 
		 * } if (!_rng[jj] && !_grp[jj]) outl += _map[jj][ntmp]; else { for (int
		 * b = 0; b < _ncols; b++) { if (_bnd[b] != null) { for (int h = 0; h <=
		 * _bnd[b].length; h++) { if (ntmp == h) { if (h == 0) { outl +=
		 * "[value <" + _bnd[b][h] + "]"; break; } if (h > 0 && h <
		 * _bnd[b].length) { outl += "[" + _bnd[b][h - 1] + "<= value <" +
		 * _bnd[b][h] + "]"; break; } if (h == _bnd[b].length) { outl +=
		 * "[value >=" + _bnd[b][h - 1] + "]"; break; } } } } if (_group[b] !=
		 * null) { for (int g = 0; g <= _group[b].length; g++) { if (ntmp==g) {
		 * outl+="Group [" + g + "] Values [" + _group[b][g] + "]"; break; } } }
		 * } } if (jj < _ncols - 1) outl += ","; }
		 * 
		 * write(_fileNameMissing,outl);
		 * 
		 * 
		 * }
		 * 
		 * 
		 * 
		 * } catch(Exception ex) { throw ex; }
		 */

	}

	private void FiveWay() {
		long n_tot_tway_cov = 0; // drk121109
		int i, j, k, r, x, ni, nj, nk, nr, nx, m, ti, tj, tk, tr;
		long[] varvalStats5 = new long[NBINS + 1]; // used to be int idm
		long n_varvalconfigs5;
		long nComs = 0; // number of combinations = C(ncols, t) //drk121109
		long tot_varvalconfigs5 = 0; // drk121109

		out_test = new int[_NGenTests][];
		Nout_test = 0;

		for (i = 0; i < NBINS + 1; i++)
			varvalStats5[i] = 0;

		CSolver validcomb = new CSolver();
		validcomb.SetConstraints(_constraints);
		validcomb.SetParameter(_parameters);

		_aInvalidComb = new ArrayList<String[][]>();
		_aInvalidNotIn = new ArrayList<String[][]>();

		for (i = _start; i < _end; i++) {
			for (j = i + 1; j < _ncols - 3; j++) {
				for (k = j + 1; k < _ncols - 2; k++) {
					for (r = k + 1; r < _ncols - 1; r++) {

						for (x = r + 1; x < _ncols; x++) {

							nComs++;

							int[][][][][] comcount = new int[_nvals[i]][][][][]; // allow
																					// row
																					// 0
																					// for
																					// #
																					// values
																					// per
																					// parm
							for (ti = 0; ti < _nvals[i]; ti++) {
								comcount[ti] = new int[_nvals[j]][][][];
								for (tj = 0; tj < _nvals[j]; tj++) {
									comcount[ti][tj] = new int[_nvals[k]][][];
									for (tk = 0; tk < _nvals[k]; tk++) {
										comcount[ti][tj][tk] = new int[_nvals[r]][];

										for (tr = 0; tr < _nvals[r]; tr++) {
											comcount[ti][tj][tk][tr] = new int[_nvals[x]];
										}
									}
								}
							}

							// forall t-way combinations of input variable
							// values:

							// comcount i,j == 0
							// for the combination designated by i,j increment
							// counts
							for (m = 0; m < _nrows; m++) { // mark if the
															// var-val config is
															// covered by the
															// tests
															// comcount[_test[m][i]][_test[m][j]][_test[m][k]][_test[m][r]][_test[m][x]]
															// += 1;
															// coumcount i,j ==
															// 1 iff some tests
															// contains tuple
															// i,j
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

								pars[0][1] = _parameters.get(i).getValues().get(_test[m][i]);
								pars[1][1] = _parameters.get(j).getValues().get(_test[m][j]);
								pars[2][1] = _parameters.get(k).getValues().get(_test[m][k]);
								pars[3][1] = _parameters.get(r).getValues().get(_test[m][r]);
								pars[4][1] = _parameters.get(x).getValues().get(_test[m][x]);

								if (_constraints.size() > 0) {
									if (validcomb.EvaluateCombination(pars))
										comcount[_test[m][i]][_test[m][j]][_test[m][k]][_test[m][r]][_test[m][x]] += 1;
									else
										comcount[_test[m][i]][_test[m][j]][_test[m][k]][_test[m][r]][_test[m][x]] += -1;

								} else
									comcount[_test[m][i]][_test[m][j]][_test[m][k]][_test[m][r]][_test[m][x]] += 1;

							}

							int varval_cnt = 0;
							int invalidComb = 0;
							int invalidcombNotCovered = 0;
							// count how many value configs are contained in a
							// test
							for (ni = 0; ni < _nvals[i]; ni++) {
								for (nj = 0; nj < _nvals[j]; nj++) {
									for (nk = 0; nk < _nvals[k]; nk++) {
										for (nr = 0; nr < _nvals[r]; nr++) {
											for (nx = 0; nx < _nvals[x]; nx++) {
												if (comcount[ni][nj][nk][nr][nx] > 0) {
													varval_cnt++;
													n_tot_tway_cov++;
												} else {
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

													if (comcount[ni][nj][nk][nr][nx] <= -1) {
														invalidComb += 1;
														_aInvalidComb.add(pars);
													}

													if (comcount[ni][nj][nk][nr][nx] == 0 && _constraints.size() > 0) {
														if (!validcomb.EvaluateCombination(pars)) {
															invalidcombNotCovered += 1;
															_aInvalidNotIn.add(pars);
															comcount[ni][nj][nk][nr][nx] = -2;
														}
													}
												}
											}
										}
									}
								}
							}
							n_varvalconfigs5 = _nvals[i] * _nvals[j] * _nvals[k] * _nvals[r] * _nvals[x];
							n_varvalconfigs5 -= (invalidComb + invalidcombNotCovered);
							tot_varvalconfigs5 += n_varvalconfigs5;

							double varval_cov = (double) varval_cnt / (double) n_varvalconfigs5;
							// varval_cov bins give the number of var/val
							// configurations covered
							// at the levels: 0, [5,10), [10,15) etc. (assume 20
							// bins)
							for (int b = 0; b <= NBINS; b++)
								if (varval_cov >= (double) b / (double) NBINS)
									varvalStats5[b]++;

							// *********** For missing combinations
							if (_GenTests && varval_cov < _minCov) {
								for (ni = 0; ni < _nvals[i]; ni++) {
									for (nj = 0; nj < _nvals[j]; nj++) {
										for (nk = 0; nk < _nvals[k]; nk++) {
											for (nr = 0; nr < _nvals[r]; nr++) {
												for (nx = 0; nx < _nvals[x]; nx++) {
													if (comcount[ni][nj][nk][nr][nx] == 0) { // not
																								// covered;
																								// write
																								// out
																								// combination
																								// to
																								// cover
														if (_rptMissingCom) {
															String outl = i + "," + j + "," + k + "," + r + "," + x
																	+ " = " + ni + "," + nj + "," + nk + "," + nr + ","
																	+ nx + " ||" + _map[i][ni] + "," + _map[j][nj] + ","
																	+ _map[k][nk] + "," + _map[r][nr] + ","
																	+ _map[x][nx] + "\n";

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

														// value i may be
														// covered but not value
														// j
														/*
														 * Boolean comout =
														 * false; int w; for (w
														 * = 0; w < Nout_test &&
														 * !comout; w++) { if
														 * (out_test[w][i] < 0
														 * && out_test[w][j] < 0
														 * && out_test[w][k] < 0
														 * && out_test[w][r] < 0
														 * && out_test[w][x]<0)
														 * { out_test[w][i] =
														 * ni; out_test[w][j] =
														 * nj; out_test[w][k] =
														 * nk; out_test[w][r] =
														 * nr; out_test[w][x] =
														 * nx; comout = true; }
														 * if (out_test[w][i] ==
														 * ni && out_test[w][j]
														 * < 0 && out_test[w][k]
														 * < 0 && out_test[w][r]
														 * < 0 && out_test[w][x]
														 * < 0) { out_test[w][j]
														 * = nj; out_test[w][k]
														 * = nk; out_test[w][r]
														 * = nr; out_test[w][x]
														 * = nx; comout = true;
														 * } if (out_test[w][i]
														 * < 0 && out_test[w][j]
														 * == nj &&
														 * out_test[w][k] < 0 &&
														 * out_test[w][r] < 0 &&
														 * out_test[w][x] < 0) {
														 * out_test[w][i] = ni;
														 * out_test[w][k] = nk;
														 * out_test[w][r] = nr;
														 * out_test[w][x] = nx;
														 * comout = true; } if
														 * (out_test[w][i] < 0
														 * && out_test[w][j] < 0
														 * && out_test[w][k] ==
														 * nk && out_test[w][r]
														 * < 0 && out_test[w][x]
														 * < 0) { out_test[w][i]
														 * = ni; out_test[w][j]
														 * = nj; out_test[w][r]
														 * = nr; out_test[w][x]
														 * = nx; comout = true;
														 * }
														 * 
														 * if (out_test[w][i] <
														 * 0 && out_test[w][j] <
														 * 0 && out_test[w][k] <
														 * 0 && out_test[w][r]
														 * == nr &&
														 * out_test[w][x] < 0) {
														 * out_test[w][i] = ni;
														 * out_test[w][j] = nj;
														 * out_test[w][k] = nk;
														 * out_test[w][x] = nx;
														 * comout = true; }
														 * 
														 * if (out_test[w][i] <
														 * 0 && out_test[w][j] <
														 * 0 && out_test[w][k] <
														 * 0 && out_test[w][r] <
														 * 0 &&
														 * out_test[w][x]==nx) {
														 * out_test[w][i] = ni;
														 * out_test[w][j] = nj;
														 * out_test[w][k] = nk;
														 * out_test[w][r] = nr;
														 * comout = true; }
														 * 
														 * 
														 * if (out_test[w][i] ==
														 * ni && out_test[w][j]
														 * == nj &&
														 * out_test[w][k] == nk
														 * && out_test[w][r] ==
														 * nr && out_test[w][x]
														 * == nx) { comout =
														 * true; } } if (!comout
														 * && Nout_test <
														 * _MaxGenTests) { //
														 * com was not output to
														 * out_test[w] = new
														 * int[_ncols]; for (int
														 * ii = 0; ii < _ncols;
														 * ii++) out_test[w][ii]
														 * = -1; out_test[w][i]
														 * = ni; out_test[w][j]
														 * = nj; out_test[w][k]
														 * = nk; out_test[w][r]
														 * = nr; out_test[w][x]
														 * = nx; Nout_test++; }
														 */
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
			/*
			 * if (_barra.isIndeterminate()) _barra.setIndeterminate(false);
			 * _barra.setValue(_barra.getValue()+1);
			 */
		}

		_n_tot_tway_cov = n_tot_tway_cov;
		_nComs = nComs;
		_tot_varvalconfig = tot_varvalconfigs5;
		_varvalStatN = varvalStats5;

		/*
		 * if (_GenTests) try {
		 * 
		 * if (_appendTests) { // append new tests to original, so write out
		 * original first for (int ii = 0; ii < _nrows; ii++) { String outl =
		 * ""; for (int jj = 0; jj < _ncols; jj++) { outl +=
		 * _map[jj][_test[ii][jj]];
		 * 
		 * if (jj < _ncols - 1) outl += ","; } write(_fileNameMissing,outl);
		 * 
		 * 
		 * } } for (int ii = 0; ii < Nout_test; ii++) { String outl = ""; for
		 * (int jj = 0; jj < _ncols; jj++) { int ntmp = out_test[ii][jj];
		 * String[][] parV; if (ntmp>0 && _constraints.size() > 0) { parV = new
		 * String[1][]; parV[0] = new String[2]; parV[0][0] =
		 * _parameters.get(jj).getName(); parV[0][1] =
		 * (String)_parameters.get(jj).getValues().get(ntmp);
		 * 
		 * if (!validcomb.EvaluateCombination(parV)) ntmp=-1; }
		 * 
		 * if (ntmp < 0) { if (_constraints.size() > 0) //if there are
		 * constraints the valid value will be search {
		 * 
		 * do { ntmp++; parV = new String[1][]; parV[0] = new String[2];
		 * parV[0][0] = _parameters.get(jj).getName(); parV[0][1] =
		 * (String)_parameters.get(jj).getValues().get(ntmp); } while
		 * (!validcomb.EvaluateCombination(parV)); } else ntmp = 0;
		 * 
		 * } if (!_rng[jj] && !_grp[jj]) outl += _map[jj][ntmp]; else { for (int
		 * b = 0; b < _ncols; b++) { if (_bnd[b] != null) { for (int h = 0; h <=
		 * _bnd[b].length;h++) { if (ntmp == h) { if (h == 0) { outl +=
		 * "[value <" + _bnd[b][h] + "]"; break; } if (h > 0 && h <
		 * _bnd[b].length) { outl += "[" + _bnd[b][h - 1] + "<= value <" +
		 * _bnd[b][h] + "]"; break; } if (h == _bnd[b].length) { outl +=
		 * "[value >=" + _bnd[b][h - 1] + "]"; break; } } } }
		 * 
		 * if (_group[b] != null) { for (int g = 0; g <= _group[b].length; g++)
		 * { if (ntmp==g) { outl+="Group [" + g + "] Values [" + _group[b][g] +
		 * "]"; break; } } } } } if (jj < _ncols - 1) outl += ","; }
		 * 
		 * write(_fileNameMissing,outl);
		 * 
		 * }
		 * 
		 * 
		 * } catch(Exception ex) { throw ex; }
		 */

	}

	private void SixWay() {

		long n_tot_tway_cov = 0; // drk121109
		int i, j, k, r, x, z, ni, nj, nk, nr, nx, nz, m, ti, tj, tk, tr, tx;
		long[] varvalStats6 = new long[NBINS + 1];
		long n_varvalconfigs6;
		long nComs = 0; // number of combinations = C(ncols, t) //drk121109
		long tot_varvalconfigs6 = 0; // drk121109

		out_test = new int[_NGenTests][];
		Nout_test = 0;

		for (i = 0; i < NBINS + 1; i++)
			varvalStats6[i] = 0;

		CSolver validcomb = new CSolver();
		validcomb.SetConstraints(_constraints);
		validcomb.SetParameter(_parameters);

		_aInvalidComb = new ArrayList<String[][]>();
		_aInvalidNotIn = new ArrayList<String[][]>();

		for (i = _start; i < _end; i++) {

			for (j = i + 1; j < _ncols - 4; j++) {
				for (k = j + 1; k < _ncols - 3; k++) {
					for (r = k + 1; r < _ncols - 2; r++) {

						for (x = r + 1; x < _ncols - 1; x++) {
							for (z = x + 1; z < _ncols; z++) {

								nComs++;

								int[][][][][][] comcount = new int[_nvals[i]][][][][][]; // allow
																							// row
																							// 0
																							// for
																							// #
																							// values
																							// per
																							// parm
								for (ti = 0; ti < _nvals[i]; ti++) {
									comcount[ti] = new int[_nvals[j]][][][][];
									for (tj = 0; tj < _nvals[j]; tj++) {
										comcount[ti][tj] = new int[_nvals[k]][][][];
										for (tk = 0; tk < _nvals[k]; tk++) {
											comcount[ti][tj][tk] = new int[_nvals[r]][][];

											for (tr = 0; tr < _nvals[r]; tr++) {
												comcount[ti][tj][tk][tr] = new int[_nvals[x]][];
												for (tx = 0; tx < _nvals[x]; tx++) {
													comcount[ti][tj][tk][tr][tx] = new int[_nvals[z]];
												}
											}
										}
									}
								}

								// forall t-way combinations of input variable
								// values:
								//

								// comcount i,j == 0
								// for the combination designated by i,j
								// increment counts
								for (m = 0; m < _nrows; m++) { // mark if the
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

									pars[0][1] = _parameters.get(i).getValues().get(_test[m][i]);
									pars[1][1] = _parameters.get(j).getValues().get(_test[m][j]);
									pars[2][1] = _parameters.get(k).getValues().get(_test[m][k]);
									pars[3][1] = _parameters.get(r).getValues().get(_test[m][r]);
									pars[4][1] = _parameters.get(x).getValues().get(_test[m][x]);
									pars[5][1] = _parameters.get(z).getValues().get(_test[m][z]);

									if (_constraints.size() > 0) {
										if (validcomb.EvaluateCombination(pars))
											comcount[_test[m][i]][_test[m][j]][_test[m][k]][_test[m][r]][_test[m][x]][_test[m][z]] += 1;
										else
											comcount[_test[m][i]][_test[m][j]][_test[m][k]][_test[m][r]][_test[m][x]][_test[m][z]] += -1;

									} else
										comcount[_test[m][i]][_test[m][j]][_test[m][k]][_test[m][r]][_test[m][x]][_test[m][z]] += 1;
								}

								int varval_cnt = 0;
								int invalidComb = 0;
								int invalidcombNotCovered = 0;
								// count how many value configs are contained in
								// a test
								for (ni = 0; ni < _nvals[i]; ni++) {
									for (nj = 0; nj < _nvals[j]; nj++) {
										for (nk = 0; nk < _nvals[k]; nk++) {
											for (nr = 0; nr < _nvals[r]; nr++) {
												for (nx = 0; nx < _nvals[x]; nx++) {
													for (nz = 0; nz < _nvals[z]; nz++) {
														if (comcount[ni][nj][nk][nr][nx][nz] > 0) {
															varval_cnt++;
															n_tot_tway_cov++;
														} else {
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

															if (comcount[ni][nj][nk][nr][nx][nz] <= -1) {
																invalidComb += 1;
																_aInvalidComb.add(pars);
															}

															if (comcount[ni][nj][nk][nr][nx][nz] == 0
																	&& _constraints.size() > 0) {
																if (!validcomb.EvaluateCombination(pars)) {
																	invalidcombNotCovered += 1;
																	_aInvalidNotIn.add(pars);
																	comcount[ni][nj][nk][nr][nx][nz] = -2;
																}

															}
														}
													}
												}
											}
										}
									}
								}
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
									if (varval_cov >= (double) b / (double) NBINS)
										varvalStats6[b]++;

								// *********** For missing combinations
								if (_GenTests && varval_cov < _minCov) {
									for (ni = 0; ni < _nvals[i]; ni++) {
										for (nj = 0; nj < _nvals[j]; nj++) {
											for (nk = 0; nk < _nvals[k]; nk++) {
												for (nr = 0; nr < _nvals[r]; nr++) {
													for (nx = 0; nx < _nvals[x]; nx++) {
														for (nz = 0; nz < _nvals[z]; nz++) {
															if (comcount[ni][nj][nk][nr][nx][nz] == 0) { // not
																											// covered;
																											// write
																											// out
																											// combination
																											// to
																											// cover
																if (_rptMissingCom) {
																	String outl = i + "," + j + "," + k + "," + r + ","
																			+ x + "," + z + " = " + ni + "," + nj + ","
																			+ nk + "," + nr + "," + nx + "," + nz
																			+ " ||" + _map[i][ni] + "," + _map[j][nj]
																			+ "," + _map[k][nk] + "," + _map[r][nr]
																			+ "," + _map[x][nx] + "," + _map[x][nz]
																			+ "\n";
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

																// value i may
																// be covered
																// but not value
																// j
																/*
																 * Boolean
																 * comout =
																 * false; int w;
																 * for (w = 0; w
																 * < Nout_test
																 * && !comout;
																 * w++) { if
																 * (out_test[w][
																 * i] < 0 &&
																 * out_test[w][
																 * j] < 0 &&
																 * out_test[w][
																 * k] < 0 &&
																 * out_test[w][
																 * r] < 0 &&
																 * out_test[w][
																 * x] < 0 &&
																 * out_test[w][
																 * z] < 0) {
																 * out_test[w][
																 * i] = ni;
																 * out_test[w][
																 * j] = nj;
																 * out_test[w][
																 * k] = nk;
																 * out_test[w][
																 * r] = nr;
																 * out_test[w][
																 * x] = nx;
																 * out_test[w][
																 * z] = nz;
																 * comout =
																 * true; } if
																 * (out_test[w][
																 * i] == ni &&
																 * out_test[w][
																 * j] < 0 &&
																 * out_test[w][
																 * k] < 0 &&
																 * out_test[w][
																 * r] < 0 &&
																 * out_test[w][
																 * x] < 0 &&
																 * out_test[w][
																 * z] < 0) {
																 * out_test[w][
																 * j] = nj;
																 * out_test[w][
																 * k] = nk;
																 * out_test[w][
																 * r] = nr;
																 * out_test[w][
																 * x] = nx;
																 * out_test[w][
																 * z] = nz;
																 * comout =
																 * true; } if
																 * (out_test[w][
																 * i] < 0 &&
																 * out_test[w][
																 * j] == nj &&
																 * out_test[w][
																 * k] < 0 &&
																 * out_test[w][
																 * r] < 0 &&
																 * out_test[w][
																 * x] < 0 &&
																 * out_test[w][
																 * z] < 0) {
																 * out_test[w][
																 * i] = ni;
																 * out_test[w][
																 * k] = nk;
																 * out_test[w][
																 * r] = nr;
																 * out_test[w][
																 * x] = nx;
																 * out_test[w][
																 * z] = nz;
																 * comout =
																 * true; } if
																 * (out_test[w][
																 * i] < 0 &&
																 * out_test[w][
																 * j] < 0 &&
																 * out_test[w][
																 * k] == nk &&
																 * out_test[w][
																 * r] < 0 &&
																 * out_test[w][
																 * x] < 0 &&
																 * out_test[w][
																 * z] < 0) {
																 * out_test[w][
																 * i] = ni;
																 * out_test[w][
																 * j] = nj;
																 * out_test[w][
																 * r] = nr;
																 * out_test[w][
																 * x] = nx;
																 * out_test[w][
																 * z] = nz;
																 * comout =
																 * true; }
																 * 
																 * if
																 * (out_test[w][
																 * i] < 0 &&
																 * out_test[w][
																 * j] < 0 &&
																 * out_test[w][
																 * k] < 0 &&
																 * out_test[w][
																 * r] == nr &&
																 * out_test[w][
																 * x] < 0 &&
																 * out_test[w][
																 * z] < 0) {
																 * out_test[w][
																 * i] = ni;
																 * out_test[w][
																 * j] = nj;
																 * out_test[w][
																 * k] = nk;
																 * out_test[w][
																 * x] = nx;
																 * out_test[w][
																 * z] = nz;
																 * comout =
																 * true; }
																 * 
																 * if
																 * (out_test[w][
																 * i] < 0 &&
																 * out_test[w][
																 * j] < 0 &&
																 * out_test[w][
																 * k] < 0 &&
																 * out_test[w][
																 * r] < 0 &&
																 * out_test[w][
																 * x] == nx &&
																 * out_test[w][
																 * z] < 0) {
																 * out_test[w][
																 * i] = ni;
																 * out_test[w][
																 * j] = nj;
																 * out_test[w][
																 * k] = nk;
																 * out_test[w][
																 * r] = nr;
																 * out_test[w][
																 * z] = nz;
																 * comout =
																 * true; }
																 * 
																 * if
																 * (out_test[w][
																 * i] < 0 &&
																 * out_test[w][
																 * j] < 0 &&
																 * out_test[w][
																 * k] < 0 &&
																 * out_test[w][
																 * r] < 0 &&
																 * out_test[w][
																 * x] < 0 &&
																 * out_test[w][
																 * z] == nz) {
																 * out_test[w][
																 * i] = ni;
																 * out_test[w][
																 * j] = nj;
																 * out_test[w][
																 * k] = nk;
																 * out_test[w][
																 * r] = nr;
																 * out_test[w][
																 * x] = nx;
																 * comout =
																 * true; }
																 * 
																 * if
																 * (out_test[w][
																 * i] == ni &&
																 * out_test[w][
																 * j] == nj &&
																 * out_test[w][
																 * k] == nk &&
																 * out_test[w][
																 * r] == nr &&
																 * out_test[w][
																 * x] == nx &&
																 * out_test[w][
																 * z] == nz) {
																 * comout =
																 * true; } } if
																 * (!comout &&
																 * Nout_test <
																 * _MaxGenTests)
																 * { // com was
																 * not output to
																 * out_test[w] =
																 * new
																 * int[_ncols];
																 * for (int ii =
																 * 0; ii <
																 * _ncols; ii++)
																 * out_test[w][
																 * ii] = -1;
																 * out_test[w][
																 * i] = ni;
																 * out_test[w][
																 * j] = nj;
																 * out_test[w][
																 * k] = nk;
																 * out_test[w][
																 * r] = nr;
																 * out_test[w][
																 * x] = nx;
																 * out_test[w][
																 * z] = nz;
																 * Nout_test++;
																 * }
																 */
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
			/*
			 * if (_barra.isIndeterminate()) _barra.setIndeterminate(false);
			 * 
			 * _barra.setValue(_barra.getValue()+1);
			 */
		}

		_n_tot_tway_cov = n_tot_tway_cov;
		_nComs = nComs;
		_tot_varvalconfig = tot_varvalconfigs6;
		_varvalStatN = varvalStats6;

		/*
		 * if (_GenTests) try { if (_appendTests) { // append new tests to
		 * original, so write out original first for (int ii = 0; ii < _nrows;
		 * ii++) { String outl = ""; for (int jj = 0; jj < _ncols; jj++) { outl
		 * += _map[jj][_test[ii][jj]];
		 * 
		 * if (jj < _ncols - 1) outl += ","; }
		 * 
		 * write(_fileNameMissing,outl);
		 * 
		 * } } for (int ii = 0; ii < Nout_test; ii++) { String outl = ""; for
		 * (int jj = 0; jj < _ncols; jj++) { int ntmp = out_test[ii][jj]; if
		 * (ntmp < 0) { if (_constraints.size() > 0) //if there are constraints
		 * the valid value will be search { String[][] parV; do { ntmp++; parV =
		 * new String[1][]; parV[0] = new String[2]; parV[0][0] =
		 * _parameters.get(jj).getName(); parV[0][1] =
		 * (String)_parameters.get(jj).getValues().get(ntmp); } while
		 * (!validcomb.EvaluateCombination(parV)); } else ntmp = 0;
		 * 
		 * } if (!_rng[jj] && !_grp[jj]) outl += _map[jj][ntmp]; else { for (int
		 * b = 0; b < _ncols; b++) { if (_bnd[b] != null) { for (int h = 0; h <=
		 * _bnd[b].length; h++) { if (ntmp == h) { if (h == 0) { outl +=
		 * "[value <" + _bnd[b][h] + "]"; break; } if (h > 0 && h <
		 * _bnd[b].length) { outl += "[" + _bnd[b][h - 1] + "<= value <" +
		 * _bnd[b][h] + "]"; break; } if (h == _bnd[b].length) { outl +=
		 * "[value >=" + _bnd[b][h - 1] + "]"; break; } } } }
		 * 
		 * if (_group[b] != null) { for (int g = 0; g <= _group[b].length; g++)
		 * { if (ntmp==g) { outl+="Group [" + g + "] Values [" + _group[b][g] +
		 * "]"; break; } } } } } if (jj < _ncols - 1) outl += ","; }
		 * 
		 * write(_fileNameMissing,outl);
		 * 
		 * 
		 * 
		 * }
		 * 
		 * 
		 * 
		 * } catch(Exception ex) { throw ex; }
		 */
	}

	@Override
	protected Object compute() {

		if (!_parallel) {

			out_test = new int[_NGenTests][];
			Nout_test = 0;

			GetTway();

			if (!wait) {
				switch (_tway) {
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
					GenerateMissingFile(out_test, Nout_test);
			}
			return 1;
		}

		int cores = Runtime.getRuntime().availableProcessors();

		Tway[] list = new Tway[cores];

		int piece = (_end) / cores;

		for (int i = 0; i < cores; i++) {
			int start = i * piece + 0;
			int end = (i == cores - 1) ? (int) (_end) : start + piece;

			list[i] = new Tway(_tway, start, end, _test, _nvals, _nrows, _ncols, _parameters, _constraints, _map);

			list[i].set_bnd(_bnd);
			list[i].set_Rng(_rng);
			list[i].set_grp(_grp);
			list[i].set_wait(true);

			if (_GenTests) {
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
		for (int i = 0; i < _ncols; i++) {
			hm_colors2[i] = new int[_ncols];
		}

		/*
		hm_colors3 = new int[_ncols][][];
		for (int i = 0; i < _ncols; i++) {
			hm_colors3[i] = new int[_ncols][];
			for (int j = 0; j < _ncols; j++) {
				hm_colors3[i][j] = new int[_ncols];
			}
		}
		*/

		_aInvalidComb = new ArrayList<String[][]>();
		_aInvalidNotIn = new ArrayList<String[][]>();

		out_test = new int[_NGenTests][];
		Nout_test = 0;

		for (int i = 0; i < cores; i++) {
			list[i].join();
			_n_tot_tway_cov = _n_tot_tway_cov + list[i]._n_tot_tway_cov;
			_nComs = _nComs + list[i]._nComs;
			_tot_varvalconfig = _tot_varvalconfig + list[i]._tot_varvalconfig;

			// if (_barra.getValue()<_end){ _barra.setValue(_end);
			// _barra.repaint();}

			for (int comb = 0; comb < list[i]._aInvalidComb.size(); comb++) {
				_aInvalidComb.add(list[i]._aInvalidComb.get(comb));
			}

			for (int comb = 0; comb < list[i]._aInvalidNotIn.size(); comb++) {
				_aInvalidNotIn.add(list[i]._aInvalidNotIn.get(comb));
			}

			for (int x = 0; x < list[i]._varvalStatN.length; x++) {
				_varvalStatN[x] = _varvalStatN[x] + list[i]._varvalStatN[x];
			}

			if (_tway == "2way") {
				for (int ii = 0; ii < _ncols; ii++)
					for (int jj = 0; jj < _ncols; jj++)
						hm_colors2[ii][jj] = hm_colors2[ii][jj] + list[i].hm_colors2[ii][jj];

				two_way_missing(list[i]._missing);
			}

			if (_tway == "3way") {
				for (int ii = 0; ii < _ncols; ii++)
					for (int jj = 0; jj < _ncols; jj++)
						//for (int kk = 0; kk < _ncols; kk++)
							//hm_colors3[ii][jj][kk] = hm_colors3[ii][jj][kk] + list[i].hm_colors3[ii][jj][kk];

				three_way_missing(list[i]._missing);
			}

			if (_tway == "4way")
				four_way_missing(list[i]._missing);

			if (_tway == "5way")
				five_way_missing(list[i]._missing);

			if (_tway == "6way")
				six_way_missing(list[i]._missing);

		}

		if (_GenTests)
			GenerateMissingFile(out_test, Nout_test);

		return 1;
	}

	private void two_way_missing(List<int[]> l) {
		for (int ii = 0; ii < _ncols; ii++)
			for (int jj = 0; jj < _ncols; jj++) {
				for (int[] mis : l) {
					if (mis[0] == ii && mis[1] == jj) {
						Boolean comout = false;
						int r;
						for (r = 0; r < Nout_test && !comout; r++) {
							if (out_test[r][ii] < 0 && out_test[r][jj] < 0) {
								out_test[r][ii] = mis[2];
								out_test[r][jj] = mis[3];
								comout = true;
							}
							if (out_test[r][ii] == mis[2] && out_test[r][jj] < 0) {
								out_test[r][jj] = mis[3];
								comout = true;
							}
							if (out_test[r][ii] < 0 && out_test[r][jj] == mis[3]) {
								out_test[r][ii] = mis[2];
								comout = true;
							}
							if (out_test[r][ii] == mis[2] && out_test[r][jj] == mis[3]) {
								comout = true;
							}
						}
						if (!comout && Nout_test < _MaxGenTests) { // com was
																	// not
																	// output to
																	// test; add
																	// new test
							out_test[r] = new int[_ncols];
							for (int x = 0; x < _ncols; x++)
								out_test[r][x] = -1; // init empty test
							out_test[r][ii] = mis[2];
							out_test[r][jj] = mis[3];
							Nout_test++;
						}
					}
				}
			}
	}

	private void three_way_missing(List<int[]> l) {
		for (int ii = 0; ii < _ncols; ii++) {
			for (int jj = 0; jj < _ncols; jj++) {
				for (int kk = 0; kk < _ncols; kk++) {
					for (int[] mis : l) {
						if (mis[0] == ii && mis[1] == jj && mis[2] == kk) {
							// value i may be covered but not value j
							Boolean comout = false;
							int r;
							for (r = 0; r < Nout_test && !comout; r++) {
								if (out_test[r][ii] < 0 && out_test[r][jj] < 0 && out_test[r][kk] < 0) {
									out_test[r][ii] = mis[3];
									out_test[r][jj] = mis[4];
									out_test[r][kk] = mis[5];
									comout = true;
								}
								if (out_test[r][ii] == mis[3] && out_test[r][jj] < 0 && out_test[r][kk] < 0) {
									out_test[r][jj] = mis[4];
									out_test[r][kk] = mis[5];
									comout = true;
								}
								if (out_test[r][ii] < 0 && out_test[r][jj] == mis[4] && out_test[r][kk] < 0) {
									out_test[r][ii] = mis[3];
									out_test[r][kk] = mis[5];
									comout = true;
								}
								if (out_test[r][ii] < 0 && out_test[r][jj] < 0 && out_test[r][kk] == mis[5]) {
									out_test[r][ii] = mis[3];
									out_test[r][jj] = mis[4];
									comout = true;
								}
								if (out_test[r][ii] == mis[3] && out_test[r][jj] == mis[4]
										&& out_test[r][kk] == mis[5]) {
									comout = true;
								}
							}
							if (!comout && Nout_test < _MaxGenTests) { // com
																		// was
																		// not
																		// output
																		// to
								out_test[r] = new int[_ncols]; // _ncols
								for (int x = 0; x < _ncols; x++)
									out_test[r][x] = -1;
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

	private void four_way_missing(List<int[]> l) {
		for (int ii = 0; ii < _ncols; ii++) {
			for (int jj = 0; jj < _ncols; jj++) {
				for (int kk = 0; kk < _ncols; kk++) {

					for (int rr = 0; rr < _ncols; rr++) {
						for (int[] mis : l) {
							if (mis[0] == ii && mis[1] == jj && mis[2] == kk && mis[3] == rr) {

								Boolean comout = false;
								int w;
								for (w = 0; w < Nout_test && !comout; w++) {
									if (out_test[w][ii] < 0 && out_test[w][jj] < 0 && out_test[w][kk] < 0
											&& out_test[w][rr] < 0) {
										out_test[w][ii] = mis[4];
										out_test[w][jj] = mis[5];
										out_test[w][kk] = mis[6];
										out_test[w][rr] = mis[7];
										comout = true;
									}
									if (out_test[w][ii] == mis[4] && out_test[w][jj] < 0 && out_test[w][kk] < 0
											&& out_test[w][rr] < 0) {
										out_test[w][jj] = mis[5];
										out_test[w][kk] = mis[6];
										out_test[w][rr] = mis[7];
										comout = true;
									}
									if (out_test[w][ii] < 0 && out_test[w][jj] == mis[5] && out_test[w][kk] < 0
											&& out_test[w][rr] < 0) {
										out_test[w][ii] = mis[4];
										out_test[w][kk] = mis[6];
										out_test[w][rr] = mis[7];
										comout = true;
									}
									if (out_test[w][ii] < 0 && out_test[w][jj] < 0 && out_test[w][kk] == mis[6]
											&& out_test[w][rr] < 0) {
										out_test[w][ii] = mis[4];
										out_test[w][jj] = mis[5];
										out_test[w][rr] = mis[7];
										comout = true;
									}

									if (out_test[w][ii] < 0 && out_test[w][jj] < 0 && out_test[w][kk] < 0
											&& out_test[w][rr] == mis[7]) {
										out_test[w][ii] = mis[4];
										out_test[w][jj] = mis[5];
										out_test[w][kk] = mis[6];
										comout = true;
									}

									if (out_test[w][ii] == mis[4] && out_test[w][jj] == mis[5]
											&& out_test[w][kk] == mis[6] && out_test[w][rr] == mis[7]) {
										comout = true;
									}
								}
								if (!comout && Nout_test < _MaxGenTests) { // com
																			// was
																			// not
																			// output
																			// to
									out_test[w] = new int[_ncols];
									for (int x = 0; x < _ncols; x++)
										out_test[w][x] = -1;
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

	private void five_way_missing(List<int[]> l) {
		for (int ii = 0; ii < _ncols; ii++) {
			for (int jj = 0; jj < _ncols; jj++) {
				for (int kk = 0; kk < _ncols; kk++) {
					for (int rr = 0; rr < _ncols; rr++) {
						for (int xx = 0; xx < _ncols; xx++) {

							for (int[] mis : l) {
								if (mis[0] == ii && mis[1] == jj && mis[2] == kk && mis[3] == rr && mis[4] == xx) {
									Boolean comout = false;
									int w;
									for (w = 0; w < Nout_test && !comout; w++) {
										if (out_test[w][ii] < 0 && out_test[w][jj] < 0 && out_test[w][kk] < 0
												&& out_test[w][rr] < 0 && out_test[w][xx] < 0) {
											out_test[w][ii] = mis[5];
											out_test[w][jj] = mis[6];
											out_test[w][kk] = mis[7];
											out_test[w][rr] = mis[8];
											out_test[w][xx] = mis[9];
											comout = true;
										}
										if (out_test[w][ii] == mis[5] && out_test[w][jj] < 0 && out_test[w][kk] < 0
												&& out_test[w][rr] < 0 && out_test[w][xx] < 0) {
											out_test[w][jj] = mis[6];
											out_test[w][kk] = mis[7];
											out_test[w][rr] = mis[8];
											out_test[w][xx] = mis[9];
											comout = true;
										}
										if (out_test[w][ii] < 0 && out_test[w][jj] == mis[6] && out_test[w][kk] < 0
												&& out_test[w][rr] < 0 && out_test[w][xx] < 0) {
											out_test[w][ii] = mis[5];
											out_test[w][kk] = mis[7];
											out_test[w][rr] = mis[8];
											out_test[w][xx] = mis[9];
											comout = true;
										}
										if (out_test[w][ii] < 0 && out_test[w][jj] < 0 && out_test[w][kk] == mis[7]
												&& out_test[w][rr] < 0 && out_test[w][xx] < 0) {
											out_test[w][ii] = mis[5];
											out_test[w][jj] = mis[6];
											out_test[w][rr] = mis[8];
											out_test[w][xx] = mis[9];
											comout = true;
										}

										if (out_test[w][ii] < 0 && out_test[w][jj] < 0 && out_test[w][kk] < 0
												&& out_test[w][rr] == mis[8] && out_test[w][xx] < 0) {
											out_test[w][ii] = mis[5];
											out_test[w][jj] = mis[6];
											out_test[w][kk] = mis[7];
											out_test[w][xx] = mis[9];
											comout = true;
										}

										if (out_test[w][ii] < 0 && out_test[w][jj] < 0 && out_test[w][kk] < 0
												&& out_test[w][rr] < 0 && out_test[w][xx] == mis[9]) {
											out_test[w][ii] = mis[5];
											out_test[w][jj] = mis[6];
											out_test[w][kk] = mis[7];
											out_test[w][rr] = mis[8];
											comout = true;
										}

										if (out_test[w][ii] == mis[5] && out_test[w][jj] == mis[6]
												&& out_test[w][kk] == mis[7] && out_test[w][rr] == mis[8]
												&& out_test[w][xx] == mis[9]) {
											comout = true;
										}
									}
									if (!comout && Nout_test < _MaxGenTests) { // com
																				// was
																				// not
																				// output
																				// to
										out_test[w] = new int[_ncols];
										for (int x = 0; x < _ncols; x++)
											out_test[w][x] = -1;
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

	private void six_way_missing(List<int[]> l) {
		for (int ii = 0; ii < _ncols; ii++) {
			for (int jj = 0; jj < _ncols; jj++) {
				for (int kk = 0; kk < _ncols; kk++) {
					for (int rr = 0; rr < _ncols; rr++) {
						for (int xx = 0; xx < _ncols; xx++) {
							for (int zz = 0; zz < _ncols; zz++) {
								for (int[] mis : l) {
									if (mis[0] == ii && mis[1] == jj && mis[2] == kk && mis[3] == rr && mis[4] == xx
											&& mis[5] == zz) {

										Boolean comout = false;
										int w;
										for (w = 0; w < Nout_test && !comout; w++) {
											if (out_test[w][ii] < 0 && out_test[w][jj] < 0 && out_test[w][kk] < 0
													&& out_test[w][rr] < 0 && out_test[w][xx] < 0
													&& out_test[w][zz] < 0) {
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
													&& out_test[w][zz] < 0) {
												out_test[w][jj] = mis[7];
												out_test[w][kk] = mis[8];
												out_test[w][rr] = mis[9];
												out_test[w][xx] = mis[10];
												out_test[w][zz] = mis[11];
												comout = true;
											}
											if (out_test[w][ii] < 0 && out_test[w][jj] == mis[7] && out_test[w][kk] < 0
													&& out_test[w][rr] < 0 && out_test[w][xx] < 0
													&& out_test[w][zz] < 0) {
												out_test[w][ii] = mis[6];
												out_test[w][kk] = mis[8];
												out_test[w][rr] = mis[9];
												out_test[w][xx] = mis[10];
												out_test[w][zz] = mis[11];
												comout = true;
											}
											if (out_test[w][ii] < 0 && out_test[w][jj] < 0 && out_test[w][kk] == mis[8]
													&& out_test[w][rr] < 0 && out_test[w][xx] < 0
													&& out_test[w][zz] < 0) {
												out_test[w][ii] = mis[6];
												out_test[w][jj] = mis[7];
												out_test[w][rr] = mis[9];
												out_test[w][xx] = mis[10];
												out_test[w][zz] = mis[11];
												comout = true;
											}

											if (out_test[w][ii] < 0 && out_test[w][jj] < 0 && out_test[w][kk] < 0
													&& out_test[w][rr] == mis[9] && out_test[w][xx] < 0
													&& out_test[w][zz] < 0) {
												out_test[w][ii] = mis[6];
												out_test[w][jj] = mis[7];
												out_test[w][kk] = mis[8];
												out_test[w][xx] = mis[10];
												out_test[w][zz] = mis[11];
												comout = true;
											}

											if (out_test[w][ii] < 0 && out_test[w][jj] < 0 && out_test[w][kk] < 0
													&& out_test[w][rr] < 0 && out_test[w][xx] == mis[10]
													&& out_test[w][zz] < 0) {
												out_test[w][ii] = mis[6];
												out_test[w][jj] = mis[7];
												out_test[w][kk] = mis[8];
												out_test[w][rr] = mis[9];
												out_test[w][zz] = mis[11];
												comout = true;
											}

											if (out_test[w][ii] < 0 && out_test[w][jj] < 0 && out_test[w][kk] < 0
													&& out_test[w][rr] < 0 && out_test[w][xx] < 0
													&& out_test[w][zz] == mis[11]) {
												out_test[w][ii] = mis[6];
												out_test[w][jj] = mis[7];
												out_test[w][kk] = mis[8];
												out_test[w][rr] = mis[9];
												out_test[w][xx] = mis[10];
												comout = true;
											}

											if (out_test[w][ii] == mis[6] && out_test[w][jj] == mis[7]
													&& out_test[w][kk] == mis[8] && out_test[w][rr] == mis[9]
													&& out_test[w][xx] == mis[10] && out_test[w][zz] == mis[11]) {
												comout = true;
											}
										}
										if (!comout && Nout_test < _MaxGenTests) { // com
																					// was
																					// not
																					// output
																					// to
											out_test[w] = new int[_ncols];
											for (int x = 0; x < _ncols; x++)
												out_test[w][x] = -1;
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

	private void GenerateMissingFile(int[][] out, int No_out) {
		try {

			if(Integer.parseInt(String.valueOf(_tway.charAt(0))) != Main.tway_max)
				return;
			CSolver validcomb = new CSolver();
			validcomb.SetConstraints(_constraints);
			validcomb.SetParameter(_parameters);
						
			//Add parameter names to file
			if (_parmName == 1) {
				String line = "";
				for (Parameter p : _parameters)
					line = line + p.getName() + ",";
				while (line.endsWith(","))
					line = line.substring(0, line.length() - 1);
				write(_fileNameMissing, line);
			}

			if (_appendTests) { // append new tests to original, so write out
								// original first

				//if (_constraints.size() > 0)
					//for (meConstraint c : _constraints)
						//write(_fileNameMissing, c.get_cons());

				for (int ii = 0; ii < _nrows; ii++) {
					String outl = "";
					for (int jj = 0; jj < _ncols; jj++) {
						outl += _map[jj][_test[ii][jj]];
						if (jj < _ncols - 1)
							outl += ",";
					}

					write(_fileNameMissing, outl);

				}
			}



			for (int ii = 0; ii < No_out; ii++) {
				String outl = "";
				for (int jj = 0; jj < _ncols; jj++) {
					int ntmp = out[ii][jj];
					String[][] parV;

					if (ntmp > 0 && _constraints.size() > 0) {
						parV = new String[1][];
						parV[0] = new String[2];
						parV[0][0] = _parameters.get(jj).getName();
						parV[0][1] = _parameters.get(jj).getValues().get(ntmp);

						if (!validcomb.EvaluateCombination(parV))
							ntmp = -1;
					}
					if (ntmp < 0) {
						// output value from input test file mapped by index
						if (_constraints.size() > 0) // if there are constraints
														// the valid value will
														// be search
						{

							do {
								ntmp++;
								parV = new String[1][];
								parV[0] = new String[2];
								parV[0][0] = _parameters.get(jj).getName();
								parV[0][1] = _parameters.get(jj).getValues().get(ntmp);
							} while (!validcomb.EvaluateCombination(parV));
						} else
							ntmp = 0; // use first index into value array for
										// this variable
					}

					// parameter doesn't have boundaries or groups specified
					if (!_rng[jj] && !_grp[jj])
						outl += _map[jj][ntmp];
					else {

						for (int b = 0; b < _ncols; b++) {
							// parameter has boundaries specified
							if (_bnd[b] != null) {
								for (int r = 0; r <= _bnd[b].length; r++) {
									if (ntmp == r) {
										if (r == 0) {
											outl += "[value <" + _bnd[b][r] + "]";
											break;
										}
										if (r > 0 && r < _bnd[b].length) {
											outl += "[" + _bnd[b][r - 1] + "<= value <" + _bnd[b][r] + "]";
											break;
										}
										if (r == _bnd[b].length) {
											outl += "[value >=" + _bnd[b][r - 1] + "]";
											break;
										}

									}
								}
							}
							// parameter has groups specified
							if (_group[b] != null) {
								for (int r = 0; r <= _group[b].length; r++) {
									if (ntmp == r) {
										outl += "Group [" + r + "] Values [" + _group[b][r] + "]";
										break;
									}
								}
							}

						}
					}

					if (jj < _ncols - 1)
						outl += ",";
				}

				write(_fileNameMissing, outl);
			}

		} catch (Exception ex) {

			throw ex;

		}
	}
	
	
	private void updateTwoWay(String[] new_tests) {

		long n_tot_tway_cov = _n_tot_tway_cov;
		int i, j, ni, nj, m, ti;
		//long[] varvalStats2 = new long[NBINS + 1];
		long n_varvalconfigs2;
		long nComs = _nComs; // number of combinations = C(ncols, t)
		long tot_varvalconfigs2 = 0;

		// prepare to create a new batch of generated tests to cover missing
		// tests

		//for (i = 0; i < NBINS + 1; i++)
			//varvalStats2[i] = 0;

		// solver for invalid combinations
		CSolver validcomb = new CSolver();
		validcomb.SetConstraints(_constraints);
		validcomb.SetParameter(_parameters);

		_aInvalidComb = new ArrayList<String[][]>();
		_aInvalidNotIn = new ArrayList<String[][]>();

		int varvaltotal = 0;

		/*
		 * -Calculates the number of t-way combinations between parameters
		 * 
		 * -Calculates the total variable value configurations
		 */
		for (i = _start; i < _end; i++) {
			for (j = i + 1; j < _ncols; j++) {
				nComs++;
				varvaltotal += (_nvals[i] * _nvals[j]);
			}
		}

		double div = (double) varvaltotal / (double) nComs;
		double sumcov = 0;
		// Process the tests
		for (i = _start; i < _end; i++) {
			for (j = i + 1; j < _ncols; j++) {
				// nComs++; //number of combinations
				int[][] comcount = new int[_nvals[i]][];
				for (ti = 0; ti < _nvals[i]; ti++) {
					comcount[ti] = new int[_nvals[j]];
				}
				// forall t-way combinations of input variable values:
				// comcount i,j == 0
				// for the combination designated by i,j increment counts
				for (m = 0; m < _nrows; m++) {

					String[][] pars = new String[2][];
					pars[0] = new String[2];
					pars[1] = new String[2];

					pars[0][0] = _parameters.get(i).getName();
					pars[1][0] = _parameters.get(j).getName();

					pars[0][1] = _parameters.get(i).getValues().get(_test[m][i]).toString();
					pars[1][1] = _parameters.get(j).getValues().get(_test[m][j]).toString();

					if (_constraints.size() > 0) {
						if (validcomb.EvaluateCombination(pars))
							comcount[_test[m][i]][_test[m][j]] += 1; // flag
																		// valid
																		// var-val
																		// config
																		// in
																		// set
																		// test
						else
							comcount[_test[m][i]][_test[m][j]] -= 1; // flag
																		// invalid
																		// var-val
																		// config
																		// in
																		// set
																		// test
					} else {
						comcount[_test[m][i]][_test[m][j]] += 1; // flag var-val
						// config in
						// set test
					}

					// coumcount i,j == 1 iff some tests contains tuple i,j
				}

				int varval_cnt = 0;
				int invalidcomb = 0;
				int invalidcombNotCovered = 0;

				for (ni = 0; ni < _nvals[i]; ni++) {
					for (nj = 0; nj < _nvals[j]; nj++) {
						// count how many value var-val configs are contained in
						// a test
						if (comcount[ni][nj] > 0) {
							varval_cnt++;
							n_tot_tway_cov++;
						} else {

							String[][] pars = new String[2][];
							pars[0] = new String[2];
							pars[1] = new String[2];

							pars[0][0] = _parameters.get(i).getName();
							pars[1][0] = _parameters.get(j).getName();

							pars[0][1] = _parameters.get(i).getValues().get(ni);
							pars[1][1] = _parameters.get(j).getValues().get(nj);

							// count how many invalid configs are contained in
							// the test
							if (comcount[ni][nj] <= -1) {
								invalidcomb += 1;
								_aInvalidComb.add(pars);
							}
							if (comcount[ni][nj] == 0 && _constraints.size() > 0) {

								if (!validcomb.EvaluateCombination(pars)) {
									// count how many invalid configs are not
									// contained in the test
									invalidcombNotCovered += 1;
									comcount[ni][nj] = -2;
									_aInvalidNotIn.add(pars);
								}

							}

						}

					}
				}

				n_varvalconfigs2 = _nvals[i] * _nvals[j];
				n_varvalconfigs2 -= (invalidcomb + invalidcombNotCovered);
				tot_varvalconfigs2 += n_varvalconfigs2;

				double varval_cov = (double) varval_cnt / (double) n_varvalconfigs2;

				double varval = (double) varval_cnt / (double) varvaltotal;

				sumcov += varval;

				//for (int b = 0; b <= NBINS; b++)
					//if (varval_cov >= (double) b / (double) NBINS)
						//varvalStats2[b]++;

			}

		}

		_n_tot_tway_cov = n_tot_tway_cov;
		_nComs = nComs;
		_tot_varvalconfig = tot_varvalconfigs2;
		//_varvalStatN = varvalStats2;

	}
	
}
