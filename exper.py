#!/usr/bin/env python
import subprocess
import os
import traceback
import sys
from subprocess import call

def main():
	commands, project_path = build_command(sys.argv)
	run_command(commands, project_path)
	parse_result(project_path)

def run_command(commands, project_path):
	os.chdir(project_path)
	print project_path
	for command in commands:
		print command
		result = subprocess.check_output(command, shell=True, stderr=None)
		print result

def parse_result(project_path):
	f = open(project_path + "/statistic.txt")
	for line in f:
		print line


def build_command(argvs):
	commands = []
	type_system_command = ""
	try:
		if len(argvs) < 4:
			raise Exception("Please provide type system and compile command")
		type_system_command = choose_type_system(argvs[1])
	except ValueError as error:
		sys.stderr.write(repr(error) + "\n")
	project_path = argvs[2]
	compile_command = " ".join(argvs[3:])
	
	dljc = os.environ['DLJC'] + "dljc" + " -t inference"
	MAXSAT_sequence = ' --solverArgs="backEndType=maxsatbackend.MaxSat,solveInParallel=false"'
	MAXSAT_parallel = ' --solverArgs="backEndType=maxsatbackend.MaxSat"'
	MAXSAT_nograph = ' --solverArgs="backEndType=maxsatbackend.MaxSat,useGraph=false,solveInParallel=false"'
	logiQL_nograph = ' --solverArgs="backEndType=logiqlbackend.LogiQL,useGraph=false"'
	logiQL_graph = ' --solverArgs="backEndType=logiqlbackend.LogiQL"'

	inference_MAXSAT_sequence = dljc + MAXSAT_sequence + type_system_command + " -- " + compile_command
	inference_MAXSAT_parallel = dljc + MAXSAT_parallel + type_system_command + " -- " + compile_command
	inference_MAXSAT_nograph = dljc + MAXSAT_nograph + type_system_command + " -- " + compile_command
	inference_logiQL_nograph = dljc + logiQL_nograph + type_system_command + " -- " + compile_command
	inference_logiQL_graph = dljc + logiQL_graph + type_system_command + " -- " + compile_command


	if argvs[1] == "dataflow":
		#commands.append(inference_MAXSAT_sequence)
		commands.append(inference_MAXSAT_parallel)
		#commands.append(inference_logiQL_graph)
	elif argvs[1]  == "ostrusted":
		commands.append(inference_MAXSAT_sequence)
		commands.append(inference_MAXSAT_parallel)
		commands.append(inference_logiQL_graph)
		commands.append(inference_logiQL_nograph)
		commands.append(inference_MAXSAT_nograph)	
	return commands, project_path

def choose_type_system(type_system_name):
	if type_system_name == "dataflow":
		return " --checker dataflow.DataflowChecker --solver dataflow.solvers.backend.DataflowConstraintSolver --mode INFER"
	elif type_system_name == "ostrusted":
		return " --checker ostrusted.OsTrustedChecker --solver constraintsolver.ConstraintSolver --mode INFER"
	else:
		raise Exception("You have to use dataflow or ostrusted as type system")

if __name__ == '__main__':
	main()