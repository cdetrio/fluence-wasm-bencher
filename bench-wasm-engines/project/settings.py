"""
Copyright 2018 Fluence Labs Limited

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""
from project.VMDescriptor import VMDescriptor

# export function name that should be called from each Wasm module
test_export_function_name = "main"

"""
    Attributes
    ----------
    vm_relative_binary_path : str
        A relative path to VM binary in its main folder.
    vm_launch_cmd : str
        An format string with command for launch this vm with provided test.

    VMDescriptor(vm_relative_binary_path="", vm_launch_cmd="")
"""

vm_descriptors = {
    "wagon"  : VMDescriptor("/engines/wagon/cmd/wasm-run/wasm-run", "{wasm_file_path}"),

    "wabt"   : VMDescriptor("/engines/wabt/bin/wasm-interp", "{wasm_file_path} --run-all-exports"),

    "v8-liftoff" : VMDescriptor("/engines/node/node", "--liftoff --no-wasm-tier-up /engines/node/node-timer.js {wasm_file_path}"),

    "v8-turbofan" : VMDescriptor("/engines/node/node", "--no-liftoff /engines/node/node-timer.js {wasm_file_path}"),

    "v8-interpreter" : VMDescriptor("/engines/node/node", "--wasm-interpret-all --liftoff --no-wasm-tier-up /engines/node/node-timer.js {wasm_file_path}"),

    "wasmtime": VMDescriptor("/engines/wasmtime/target/release/wasmtime", "{wasm_file_path} --invoke=main"),

    "wavm"   : VMDescriptor("/engines/wavm-build/bin/wavm-run", "{wasm_file_path} -f {function_name}"),

    "life-polymerase" : VMDescriptor("/engines/life/life", "-polymerase -entry {function_name} {wasm_file_path}"),

    "life"   : VMDescriptor("/engines/life/life", "-entry {function_name} {wasm_file_path}"),

    "wasmi"  : VMDescriptor("/engines/wasmi/target/release/examples/invoke", "{wasm_file_path} {function_name}"),

    "asmble" : VMDescriptor("/engines/asmble/bin/asmble", "invoke -in {wasm_file_path} {function_name} -defmaxmempages 20000")

    # "wasmer" : VMDescriptor("/engines/wasmer/target/release/wasmer", "run {wasm_file_path}", True),
    # we have binaryen, but calling wasm-shell -e main is not working
}
